package com.example.sumayerestaurant.ui.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.InventoryStock;
import com.example.sumayerestaurant.data.model.User;
import java.math.BigDecimal;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Compact Phase 7 landing page: summary first, then clear task actions. */
public class InventoryDashboardActivity extends AppCompatActivity {
    private TextView summary;
    @Override public void onCreate(Bundle b){super.onCreate(b);setTitle("Usimamizi wa hisa");LinearLayout root=new LinearLayout(this);root.setPadding(32,32,32,32);root.setOrientation(LinearLayout.VERTICAL);summary=new TextView(this);summary.setText("Inapakia muhtasari wa hisa…");summary.setTextSize(18);root.addView(summary);add(root,"Orodha ya hisa",InventoryActivity.class,null);add(root,"Rekodi upotevu",StockActionActivity.class,"WASTAGE");add(root,"Rekebisha hisa",StockActionActivity.class,"ADJUSTMENT");add(root,"Simamia mapishi",RecipePurchaseActivity.class,"RECIPE");add(root,"Ununuzi wa bidhaa",RecipePurchaseActivity.class,"PURCHASE");setContentView(root);loadSummary();}
    private void add(LinearLayout root,String label,Class<?> target,String mode){Button button=new Button(this);button.setText(label);button.setMinHeight(96);root.addView(button);button.setOnClickListener(v->{Intent i=new Intent(this,target);if(mode!=null)i.putExtra(target==StockActionActivity.class?StockActionActivity.EXTRA_MODE:RecipePurchaseActivity.EXTRA_MODE,mode);startActivity(i);});}
    private void loadSummary(){User u=new TokenManager(this).getUser();Long branchId=(u!=null&&u.getBranchId()!=null)?u.getBranchId():1L;RetrofitClient.getApiService(this).getInventoryStock(branchId).enqueue(new Callback<List<InventoryStock>>(){public void onResponse(Call<List<InventoryStock>>c,Response<List<InventoryStock>>r){if(!r.isSuccessful()||r.body()==null){summary.setText("Imeshindikana kupakia muhtasari.");return;}int low=0,out=0;BigDecimal value=BigDecimal.ZERO;for(InventoryStock s:r.body()){if("STOCK NDOGO".equals(s.getStockBadge()))low++;if("IMEISHA".equals(s.getStockBadge()))out++;if(s.getQuantityOnHand()!=null&&s.getCostPerUnit()!=null)value=value.add(s.getQuantityOnHand().multiply(s.getCostPerUnit()));}summary.setText("Jumla ya bidhaa: "+r.body().size()+"\nStock ndogo: "+low+"\nZimeisha: "+out+"\nThamani ya hisa: TZS "+value); }public void onFailure(Call<List<InventoryStock>>c,Throwable t){summary.setText("Hakuna mtandao. Tafadhali jaribu tena.");}});}
}
