package com.example.sumayerestaurant.ui.inventory;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.Ingredient;
import com.example.sumayerestaurant.data.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Shared, confirmation-first form for wastage and physical-count adjustments. */
public class StockActionActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "mode";
    private Spinner ingredients, direction;
    private EditText quantity, reason;
    private boolean wastage;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); wastage = "WASTAGE".equals(getIntent().getStringExtra(EXTRA_MODE));
        LinearLayout root = new LinearLayout(this); root.setPadding(32,32,32,32); root.setOrientation(LinearLayout.VERTICAL);
        setTitle(wastage ? "Rekodi upotevu" : "Rekebisha hisa");
        ingredients = new Spinner(this); root.addView(ingredients);
        quantity = new EditText(this); quantity.setHint("Kiasi (zaidi ya sifuri)"); quantity.setInputType(2|8192); root.addView(quantity);
        if (!wastage) { direction = new Spinner(this); direction.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Ongeza hisa", "Punguza hisa"})); root.addView(direction); }
        reason = new EditText(this); reason.setHint(wastage ? "Sababu ya upotevu" : "Sababu ya marekebisho"); reason.setMinLines(2); root.addView(reason);
        Button save = new Button(this); save.setText(wastage ? "Thibitisha upotevu" : "Thibitisha marekebisho"); root.addView(save); setContentView(root);
        save.setOnClickListener(v -> confirm()); loadIngredients();
    }
    private void loadIngredients() { RetrofitClient.getApiService(this).getIngredients().enqueue(new Callback<List<Ingredient>>() {
        public void onResponse(Call<List<Ingredient>> c, Response<List<Ingredient>> r) { if (r.isSuccessful() && r.body()!=null) ingredients.setAdapter(new ArrayAdapter<>(StockActionActivity.this, android.R.layout.simple_spinner_dropdown_item,r.body())); else message("Imeshindikana kupakia viungo."); }
        public void onFailure(Call<List<Ingredient>> c, Throwable t) { message("Hakuna mtandao. Tafadhali jaribu tena."); }
    }); }
    private void confirm() { if (ingredients.getSelectedItem()==null || quantity.getText().toString().trim().isEmpty() || reason.getText().toString().trim().isEmpty()) { message("Jaza kiungo, kiasi na sababu."); return; }
        Ingredient item=(Ingredient)ingredients.getSelectedItem(); new AlertDialog.Builder(this).setMessage("Una uhakika unataka kurekodi " + (wastage?"upotevu wa ":"mabadiliko ya ") + item.getName() + "?").setNegativeButton("Ghairi",null).setPositiveButton("Thibitisha",(d,w)->submit(item)).show(); }
    private void submit(Ingredient item) { User user=new TokenManager(this).getUser(); if(user==null||user.getBranchId()==null){message("Tawi halijapatikana.");return;} Map<String,Object> body=new HashMap<>(); body.put("ingredientId",item.getId()); body.put("quantity",quantity.getText().toString().trim()); body.put("unit",item.getDefaultUnit()); body.put("reason",reason.getText().toString().trim());
        Call<Object> call; if(wastage) call=RetrofitClient.getApiService(this).recordWastage(user.getBranchId(),body); else {body.put("adjustmentType",direction.getSelectedItemPosition()==0?"IN":"OUT");call=RetrofitClient.getApiService(this).adjustStock(user.getBranchId(),body);} call.enqueue(new Callback<Object>() { public void onResponse(Call<Object> c, Response<Object> r){if(r.isSuccessful()){message("Taarifa imehifadhiwa.");finish();}else message("Imeshindikana kuhifadhi. Hakikisha kiasi kinapatikana.");} public void onFailure(Call<Object> c,Throwable t){message("Hakuna mtandao. Tafadhali jaribu tena.");}}); }
    private void message(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
}
