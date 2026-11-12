package com.shivam.cb350cmileage;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.text.*;

public class MainActivity extends Activity {
  DB db; LinearLayout root, entries; TextView summary; Chart chart; EditText distance,litres,price,reading,date; long editing=-1;
  int navy=Color.rgb(16,28,46), green=Color.rgb(0,135,104); DecimalFormat fmt=new DecimalFormat("0.00");
  int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);} 
  @Override public void onCreate(Bundle b){super.onCreate(b);db=new DB(this); build(); refresh();}
  TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(navy);t.setPadding(0,dp(7),0,dp(5));return t;}
  EditText field(String hint, boolean numeric){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setTextSize(16);if(numeric)e.setInputType(8194);e.setPadding(dp(10),dp(8),dp(10),dp(8));return e;}
  void build(){ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(20),dp(18),dp(28));root.setBackgroundColor(Color.rgb(247,249,252));sc.addView(root);setContentView(sc);
    TextView title=label("CB350C  •  MILEAGE TRACKER",23);title.setTypeface(null,1);root.addView(title);
    TextView by=label("By Shivam Gauns  |  Offline fuel log",12);by.setTextColor(green);root.addView(by);
    summary=label("",16);root.addView(summary);
    chart=new Chart(this);root.addView(chart,new LinearLayout.LayoutParams(-1,dp(235)));
    TextView h=label("Add / edit a full-tank refill",19);h.setTypeface(null,1);root.addView(h);
    root.addView(label("Date (YYYY-MM-DD)",13));date=field("2026-11-12",false);date.setText(new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()));root.addView(date);
    root.addView(label("Distance since previous full tank (km)",13));distance=field("e.g. 395.8",true);root.addView(distance);
    root.addView(label("Fuel filled to full (litres)",13));litres=field("e.g. 11.74",true);root.addView(litres);
    root.addView(label("Price per litre (₹)",13));price=field("e.g. 113.80",true);root.addView(price);
    root.addView(label("Bike's displayed mileage (km/L) — optional",13));reading=field("e.g. 34.5",true);root.addView(reading);
    Button save=new Button(this);save.setText("SAVE REFILL");save.setOnClickListener(v->save());root.addView(save);
    Button cancel=new Button(this);cancel.setText("CLEAR / CANCEL EDIT");cancel.setOnClickListener(v->clear());root.addView(cancel);
    Button export=new Button(this);export.setText("EXPORT HISTORY AS CSV");export.setOnClickListener(v->export());root.addView(export);
    TextView note=label("How to measure: Reset Trip A after each full-tank refill. At the next full-tank refill, enter Trip A distance and the litres added. Keep the filling level consistent. Display mileage is optional and is not used to calculate actual mileage.",12);note.setTextColor(Color.DKGRAY);root.addView(note);
    TextView hist=label("Refuelling history",19);hist.setTypeface(null,1);root.addView(hist);entries=new LinearLayout(this);entries.setOrientation(1);root.addView(entries);
  }
  void save(){try{String dt=date.getText().toString().trim();if(!dt.matches("\\d{4}-\\d{2}-\\d{2}"))throw new Exception("Enter date as YYYY-MM-DD");SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.US);f.setLenient(false);f.parse(dt);
    double km=Double.parseDouble(distance.getText().toString()), l=Double.parseDouble(litres.getText().toString()), p=Double.parseDouble(price.getText().toString());if(km<=0||l<=0||p<0)throw new Exception("Distance and litres must be positive; price cannot be negative");
    String r=reading.getText().toString().trim();Double displayed=r.isEmpty()?null:Double.parseDouble(r);if(displayed!=null&&displayed<=0)throw new Exception("Displayed mileage must be positive");
    android.content.ContentValues cv=new android.content.ContentValues();cv.put("dt",dt);cv.put("km",km);cv.put("litres",l);cv.put("price",p);if(displayed==null)cv.putNull("reading");else cv.put("reading",displayed);
    if(editing<0)db.getWritableDatabase().insert("fills",null,cv);else db.getWritableDatabase().update("fills",cv,"id=?",new String[]{""+editing});clear();refresh();Toast.makeText(this,"Refill saved",Toast.LENGTH_SHORT).show();
  }catch(Exception ex){new AlertDialog.Builder(this).setMessage(ex.getMessage()).setPositiveButton("OK",null).show();}}
  void clear(){editing=-1;distance.setText("");litres.setText("");price.setText("");reading.setText("");date.setText(new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()));}
  ArrayList<Fill> all(){ArrayList<Fill> a=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT id,dt,km,litres,price,reading FROM fills ORDER BY dt ASC,id ASC",null);while(c.moveToNext()){Fill x=new Fill();x.id=c.getLong(0);x.dt=c.getString(1);x.km=c.getDouble(2);x.l=c.getDouble(3);x.p=c.getDouble(4);x.reading=c.isNull(5)?null:c.getDouble(5);a.add(x);}c.close();return a;}
  void refresh(){ArrayList<Fill> a=all();double km=0,l=0,cost=0;for(Fill x:a){km+=x.km;l+=x.l;cost+=x.l*x.p;}summary.setText(a.isEmpty()?"No refills yet. Add your first full-tank record below.":"OVERALL ACTUAL MILEAGE   "+fmt.format(km/l)+" km/L\nTotal distance   "+fmt.format(km)+" km\nFuel used   "+fmt.format(l)+" L\nTotal fuel cost   ₹"+fmt.format(cost)+"\nCost per km   ₹"+fmt.format(cost/km));chart.setData(a);entries.removeAllViews();ArrayList<Fill> rev=new ArrayList<>(a);Collections.reverse(rev);for(Fill x:rev){TextView t=label(x.dt+"   •   "+fmt.format(x.km/x.l)+" km/L\n"+fmt.format(x.km)+" km  /  "+fmt.format(x.l)+" L  /  ₹"+fmt.format(x.l*x.p)+(x.reading==null?"":"\nBike reading: "+fmt.format(x.reading)+" km/L  •  Difference: "+fmt.format(x.km/x.l-x.reading)+" km/L"),14);entries.addView(t);LinearLayout row=new LinearLayout(this);Button edit=new Button(this);edit.setText("Edit");edit.setOnClickListener(v->{editing=x.id;date.setText(x.dt);distance.setText(""+x.km);litres.setText(""+x.l);price.setText(""+x.p);reading.setText(x.reading==null?"":""+x.reading);Toast.makeText(this,"Editing "+x.dt+" — scroll to form",Toast.LENGTH_LONG).show();});row.addView(edit);Button del=new Button(this);del.setText("Delete");del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Delete refill?").setMessage(x.dt).setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{db.getWritableDatabase().delete("fills","id=?",new String[]{""+x.id});refresh();}).show());row.addView(del);entries.addView(row);View line=new View(this);line.setBackgroundColor(0xffdfe4eb);entries.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));}}
  void export(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("text/csv");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"cb350c_mileage_history.csv");startActivityForResult(i,101);}
  @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==101&&result==RESULT_OK&&data!=null){try{StringBuilder s=new StringBuilder("Date,Distance km,Fuel litres,Price per litre INR,Actual km per litre,Displayed km per litre,Total INR\n");for(Fill x:all())s.append(x.dt).append(',').append(x.km).append(',').append(x.l).append(',').append(x.p).append(',').append(x.km/x.l).append(',').append(x.reading==null?"":x.reading).append(',').append(x.l*x.p).append('\n');java.io.OutputStream o=getContentResolver().openOutputStream(data.getData());o.write(s.toString().getBytes("UTF-8"));o.close();Toast.makeText(this,"CSV exported",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Export failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}}}
  static class Fill{long id;String dt;double km,l,p;Double reading;}
  static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"mileage.db",null,1);}public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE fills(id INTEGER PRIMARY KEY AUTOINCREMENT,dt TEXT NOT NULL,km REAL NOT NULL,litres REAL NOT NULL,price REAL NOT NULL,reading REAL)");android.content.ContentValues v=new android.content.ContentValues();v.put("dt","2026-11-12");v.put("km",395.8);v.put("litres",11.74);v.put("price",113.80);d.insert("fills",null,v);}public void onUpgrade(SQLiteDatabase d,int old,int now){} }
  class Chart extends View{ArrayList<Fill> data=new ArrayList<>();Paint p=new Paint(3);Chart(Context c){super(c);}void setData(ArrayList<Fill> a){data=a;invalidate();}protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),left=dp(45),right=w-dp(15),top=dp(30),bottom=h-dp(38);c.drawColor(0xffffffff);p.setColor(navy);p.setTextSize(dp(13));p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Actual mileage trend (km/L)",dp(12),dp(19),p);p.setTypeface(Typeface.DEFAULT);if(data.isEmpty()){c.drawText("Chart appears after your first refill",dp(12),h/2,p);return;}double lo=Double.MAX_VALUE,hi=0;for(Fill x:data){double y=x.km/x.l;lo=Math.min(lo,y);hi=Math.max(hi,y);if(x.reading!=null){lo=Math.min(lo,x.reading);hi=Math.max(hi,x.reading);}}lo=Math.max(0,Math.floor(lo-3));hi=Math.ceil(hi+3);if(hi<=lo)hi=lo+1;p.setStrokeWidth(dp(1));p.setTextSize(dp(10));for(int i=0;i<=4;i++){float y=bottom-(bottom-top)*i/4f;p.setColor(0xffe4e8ee);c.drawLine(left,y,right,y,p);p.setColor(navy);c.drawText(fmt.format(lo+(hi-lo)*i/4),dp(2),y+dp(3),p);}for(int series=0;series<2;series++){p.setColor(series==0?green:0xffd18b21);p.setStrokeWidth(dp(2.5f));float px=0,py=0;boolean prev=false;for(int i=0;i<data.size();i++){Fill x=data.get(i);Double val=series==0?x.km/x.l:x.reading;if(val==null){prev=false;continue;}float xx=left+(right-left)*(data.size()==1?.5f:i/(float)(data.size()-1));float yy=(float)(bottom-(val-lo)/(hi-lo)*(bottom-top));if(prev)c.drawLine(px,py,xx,yy,p);c.drawCircle(xx,yy,dp(4),p);px=xx;py=yy;prev=true;}}p.setColor(green);p.setTextSize(dp(11));c.drawText("● Actual",left,bottom+dp(22),p);p.setColor(0xffd18b21);c.drawText("● Display",left+dp(90),bottom+dp(22),p);}}
}
