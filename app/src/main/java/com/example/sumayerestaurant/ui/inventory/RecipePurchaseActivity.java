package com.example.sumayerestaurant.ui.inventory;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.Ingredient;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.data.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Recipe supports several ingredients; purchase supports several purchase lines. */
public class RecipePurchaseActivity extends AppCompatActivity {
    public static final String EXTRA_MODE="mode";
    private boolean recipe; private Spinner menu, ingredient; private EditText quantity, cost; private LinearLayout lines; private final List<Map<String,Object>> items=new ArrayList<>();
    @Override public void onCreate(Bundle b){super.onCreate(b);recipe="RECIPE".equals(getIntent().getStringExtra(EXTRA_MODE));setTitle(recipe?"Mapishi ya chakula":"Ununuzi wa bidhaa");LinearLayout root=new LinearLayout(this);root.setPadding(32,32,32,32);root.setOrientation(LinearLayout.VERTICAL);
        if(recipe){menu=new Spinner(this);root.addView(menu);} ingredient=new Spinner(this);root.addView(ingredient);quantity=new EditText(this);quantity.setHint(recipe?"Kiasi kwa sahani moja":"Kiasi kilichoagizwa");quantity.setInputType(2|8192);root.addView(quantity);
        if(!recipe){cost=new EditText(this);cost.setHint("Bei kwa kipimo (TZS)");cost.setInputType(2|8192);root.addView(cost);} Button add=new Button(this);add.setText("Ongeza kwenye orodha");root.addView(add);lines=new LinearLayout(this);lines.setOrientation(LinearLayout.VERTICAL);root.addView(lines);Button save=new Button(this);save.setText(recipe?"Hifadhi mapishi":"Hifadhi ununuzi");root.addView(save);setContentView(root);add.setOnClickListener(v->addLine());save.setOnClickListener(v->confirm());loadData();}
    private void loadData(){RetrofitClient.getApiService(this).getIngredients().enqueue(new Callback<List<Ingredient>>(){public void onResponse(Call<List<Ingredient>>c,Response<List<Ingredient>>r){if(r.isSuccessful()&&r.body()!=null)ingredient.setAdapter(new ArrayAdapter<>(RecipePurchaseActivity.this,android.R.layout.simple_spinner_dropdown_item,r.body()));else msg("Imeshindikana kupakia viungo.");}public void onFailure(Call<List<Ingredient>>c,Throwable t){msg("Hakuna mtandao.");}});if(recipe){User u=new TokenManager(this).getUser();if(u==null||u.getBranchId()==null)return;RetrofitClient.getApiService(this).getMenuItems(u.getBranchId()).enqueue(new Callback<List<MenuItem>>(){public void onResponse(Call<List<MenuItem>>c,Response<List<MenuItem>>r){if(r.isSuccessful()&&r.body()!=null)menu.setAdapter(new ArrayAdapter<>(RecipePurchaseActivity.this,android.R.layout.simple_spinner_dropdown_item,r.body()));else msg("Imeshindikana kupakia chakula.");}public void onFailure(Call<List<MenuItem>>c,Throwable t){msg("Hakuna mtandao.");}});}}
    private void addLine(){if(ingredient.getSelectedItem()==null||quantity.getText().toString().trim().isEmpty()){msg("Chagua kiungo na kiasi.");return;}Ingredient i=(Ingredient)ingredient.getSelectedItem();Map<String,Object> line=new HashMap<>();line.put("ingredientId",i.getId());line.put(recipe?"quantityRequired":"quantityOrdered",quantity.getText().toString().trim());line.put("unit",i.getDefaultUnit());if(!recipe)line.put("unitCost",cost.getText().toString().trim().isEmpty()?"0":cost.getText().toString().trim());items.add(line);TextView v=new TextView(this);v.setText(i.getName()+" — "+quantity.getText()+" "+i.getDefaultUnit());v.setPadding(12,12,12,12);lines.addView(v);quantity.setText("");if(!recipe)cost.setText("");}
    private void confirm(){if(items.isEmpty()||(recipe&&menu.getSelectedItem()==null)){msg("Ongeza angalau kiungo kimoja.");return;}new AlertDialog.Builder(this).setMessage(recipe?"Hifadhi mapishi haya?":"Hifadhi ununuzi huu? Hisa itaongezwa baada ya kupokelewa.").setNegativeButton("Ghairi",null).setPositiveButton("Thibitisha",(d,w)->submit()).show();}
    private void submit(){User u=new TokenManager(this).getUser();if(u==null||u.getBranchId()==null){msg("Tawi halijapatikana.");return;}Map<String,Object> body=new HashMap<>();Call<Object> call;if(recipe){MenuItem m=(MenuItem)menu.getSelectedItem();body.put("menuItemId",m.getId());body.put("name","Mapishi ya "+m.getName());body.put("items",items);call=RetrofitClient.getApiService(this).saveRecipe(u.getBranchId(),body);}else{body.put("items",items);body.put("notes","Imehifadhiwa kupitia Android");call=RetrofitClient.getApiService(this).createPurchase(u.getBranchId(),body);}call.enqueue(new Callback<Object>(){public void onResponse(Call<Object>c,Response<Object>r){if(r.isSuccessful()){msg(recipe?"Mapishi yamehifadhiwa.":"Ununuzi umehifadhiwa. Pokea bidhaa ili kuongeza hisa.");finish();}else msg("Imeshindikana kuhifadhi. Tafadhali hakiki taarifa.");}public void onFailure(Call<Object>c,Throwable t){msg("Hakuna mtandao. Tafadhali jaribu tena.");}});}
    private void msg(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
