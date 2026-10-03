package com.alina.ai;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.speech.*;
import android.speech.tts.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, characterBox;
    ImageView alina;
    TextView status, response;
    TextToSpeech tts;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable lipLoop;
    boolean speaking = false;

    final int PINK=Color.rgb(255,55,180), PURPLE=Color.rgb(160,75,255), CYAN=Color.rgb(75,220,255);

    TextView tv(String s,float z){ TextView v=new TextView(this); v.setText(s); v.setTextColor(Color.WHITE); v.setTextSize(z); v.setGravity(Gravity.CENTER); return v; }

    GradientDrawable box(int c){
        GradientDrawable g=new GradientDrawable(); g.setColor(Color.argb(190,18,12,30)); g.setCornerRadius(28); g.setStroke(2,c); return g;
    }

    Button btn(String s,int c){
        Button b=new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setTextSize(13); b.setAllCaps(false); b.setBackground(box(c)); return b;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        tts=new TextToSpeech(this, x -> { if(x==TextToSpeech.SUCCESS) tts.setLanguage(new Locale("hi","IN")); });
        home();
        setState("idle");
        checkMicPermission();
    }

    void checkMicPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{android.Manifest.permission.RECORD_AUDIO}, 200);
        } else {
            handler.postDelayed(this::listen, 700);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 &&
            grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            handler.postDelayed(this::listen, 700);
        } else if (requestCode == 200) {
            status.setText("Boss, sunne ke liye microphone permission chahiye.");
        }
    }

    void home(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18,12,18,12); root.setBackgroundColor(Color.rgb(5,5,10)); setContentView(root);

        LinearLayout top=new LinearLayout(this);
        TextView brand=tv("🐯  ALINA AI",22); brand.setTextColor(PINK);
        top.addView(brand,new LinearLayout.LayoutParams(0,60,1)); top.addView(btn("⚙",PURPLE),new LinearLayout.LayoutParams(58,58)); root.addView(top);

        status=tv("✨  Hi Boss, I'm Alina  ✨",20); root.addView(status,new LinearLayout.LayoutParams(-1,55));

        characterBox=new LinearLayout(this); characterBox.setGravity(Gravity.CENTER); characterBox.setBackground(box(PINK));
        alina=new ImageView(this); alina.setScaleType(ImageView.ScaleType.CENTER_CROP);
        characterBox.addView(alina,new LinearLayout.LayoutParams(-1,-1));
        root.addView(characterBox,new LinearLayout.LayoutParams(-1,0,1));

        response=tv("Hello Boss ❤️ Main Alina hoon.",15); response.setTextColor(CYAN);
        root.addView(response,new LinearLayout.LayoutParams(-1,48));

        Button mic=btn("🎙️   TAP TO SPEAK",PINK); mic.setTextSize(18);
        root.addView(mic,new LinearLayout.LayoutParams(-1,68)); mic.setOnClickListener(v->listen());

        addRow(new String[]{"💬 WhatsApp","▶ YouTube","🔎 Search"},PURPLE);
        addRow(new String[]{"📱 Apps","👨‍💻 Code","🌐 Website"},PINK);
        root.addView(tv("Home     Chat     Voice     Apps     Settings",12),new LinearLayout.LayoutParams(-1,42));
    }

    void addRow(String[] ss,int c){
        LinearLayout r=new LinearLayout(this);
        for(String s:ss){
            Button b=btn(s,c); r.addView(b,new LinearLayout.LayoutParams(0,58,1));
            if(s.contains("WhatsApp")) b.setOnClickListener(v->open("com.whatsapp","https://www.whatsapp.com"));
            if(s.contains("YouTube")) b.setOnClickListener(v->openWeb("https://www.youtube.com"));
            if(s.contains("Search")) b.setOnClickListener(v->openWeb("https://www.google.com"));
        }
        root.addView(r);
    }

    void setState(String state){
        int res=getResources().getIdentifier("alina_"+state,"drawable",getPackageName());
        if(res!=0) alina.setImageResource(res);

        if(state.equals("idle")){
            status.setText("✨  Hi Boss, I'm Alina  ✨");
            response.setText("Hello Boss ❤️ Main Alina hoon.");
            alina.animate().scaleX(1.02f).scaleY(1.02f).setDuration(1400).withEndAction(()->{
                if(!speaking) alina.animate().scaleX(1f).scaleY(1f).setDuration(1400).start();
            }).start();
        } else if(state.equals("listening")){
            status.setText("🎙️  Listening to Boss…");
            response.setText("Haan Boss, main sun rahi hoon ❤️");
            pulse();
        } else if(state.equals("thinking")){
            status.setText("🧠  Thinking…");
            response.setText("Ek second Boss, main soch rahi hoon…");
            alina.animate().rotationBy(2).setDuration(500).withEndAction(()->alina.animate().rotationBy(-4).setDuration(500).start()).start();
        } else if(state.equals("speaking")){
            status.setText("🗣️  Speaking to Boss…");
            startLipSync();
        }
    }

    void pulse(){
        alina.animate().alpha(.72f).scaleX(1.03f).scaleY(1.03f).setDuration(450).withEndAction(()->{
            if(!speaking) pulse();
        }).start();
    }

    // Hindi-oriented text-to-viseme lip-sync.
    // Android TextToSpeech does not expose phoneme timestamps directly, so this
    // maps Hindi/Latin speech units to visemes and schedules them against the
    // estimated TTS duration.
    void startLipSync(){
        speaking=true;
        if(lipLoop!=null) handler.removeCallbacks(lipLoop);
        String text = response.getText().toString();
        final String[] frames = HindiVisemeEngine.framesFor(text);
        final long total = Math.max(1400L, text.length() * 58L);
        final long step = Math.max(90L, total / Math.max(1, frames.length));
        lipLoop = new Runnable(){
            int i=0;
            public void run(){
                if(!speaking) return;
                String key = frames[Math.min(i, frames.length-1)];
                int id = getResources().getIdentifier("alina_mouth_"+key,"drawable",getPackageName());
                if(id==0) id = getResources().getIdentifier("alina_speaking","drawable",getPackageName());
                if(id!=0) alina.setImageResource(id);
                i++;
                if(i < frames.length) handler.postDelayed(this, step);
                else handler.postDelayed(() -> { if(speaking) setState("idle"); }, step);
            }
        };
        handler.post(lipLoop);
    }

    static class HindiVisemeEngine {
        // A compact Hindi/Hinglish grapheme-to-viseme approximation.
        // A/Aa, E/I, O/U, M/B/P, T/D/N, S/Sh and R are represented by
        // separate mouth-shape assets when available.
        static String[] framesFor(String text){
            String s = text.toLowerCase(Locale.ROOT);
            ArrayList<String> out = new ArrayList<>();
            for(int i=0;i<s.length();i++){
                char c=s.charAt(i);
                String v;
                if("aeiou".indexOf(c)>=0) {
                    if(c=='a') v="a"; else if(c=='e'||c=='i') v="e";
                    else v="o";
                } else if("mbp".indexOf(c)>=0 || c=='म'||c=='ब'||c=='प') v="mbp";
                else if("tdn".indexOf(c)>=0 || "टडतनद".indexOf(c)>=0) v="tdn";
                else if(c=='r'||c=='र') v="r";
                else if("sz".indexOf(c)>=0 || c=='स'||c=='श'||c=='ष') v="ssh";
                else if(c=='h'||c=='ह') v="e";
                else v="neutral";
                if(!v.equals("neutral") || out.isEmpty()) out.add(v);
            }
            if(out.isEmpty()) out.add("a");
            return out.toArray(new String[0]);
        }
    }

    void stopSpeaking(){
        speaking=false;
        if(lipLoop!=null) handler.removeCallbacks(lipLoop);
        setState("idle");
    }

    void listen(){
        setState("listening");
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"hi-IN");
        try{ startActivityForResult(i,100); }
        catch(Exception e){ response.setText("Boss, voice recognition available nahi hai."); setState("idle"); }
    }

    @Override protected void onActivityResult(int r,int c,Intent d){
        super.onActivityResult(r,c,d);
        if(r==100 && c==RESULT_OK && d!=null){
            ArrayList<String> a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(a!=null && !a.isEmpty()) reply(a.get(0)); else setState("idle");
        } else setState("idle");
    }

    void reply(String q){
        setState("thinking");
        String s = q.toLowerCase(Locale.ROOT).trim();
        String ans = null;

        if(s.contains("youtube")){
            open("com.google.android.youtube","https://www.youtube.com");
            ans = "Boss, YouTube khol rahi hoon.";
        } else if(s.contains("whatsapp")){
            open("com.whatsapp","https://www.whatsapp.com");
            ans = "Boss, WhatsApp khol rahi hoon.";
        } else if(s.contains("chrome")){
            open("com.android.chrome","https://www.google.com");
            ans = "Boss, Chrome khol rahi hoon.";
        } else if(s.contains("camera") || s.contains("कैमरा")){
            Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
            try { startActivity(i); ans = "Boss, camera khol rahi hoon."; }
            catch(Exception e) { ans = "Boss, camera nahi khul paaya."; }
        } else if(s.contains("gallery") || s.contains("photos") || s.contains("गैलरी")){
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setType("image/*");
            try { startActivity(i); ans = "Boss, gallery khol rahi hoon."; }
            catch(Exception e) { ans = "Boss, gallery nahi khul paayi."; }
        } else if(s.contains("settings") || s.contains("सेटिंग")){
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                ans = "Boss, settings khol rahi hoon.";
            } catch(Exception e) { ans = "Boss, settings nahi khul paayi."; }
        } else if(s.contains("phone") || s.contains("dialer") || s.contains("फोन")){
            try {
                startActivity(new Intent(Intent.ACTION_DIAL));
                ans = "Boss, phone khol rahi hoon.";
            } catch(Exception e) { ans = "Boss, phone nahi khul paaya."; }
        } else if(s.contains("hello") || s.equals("hi") || s.contains("हाय") || s.contains("नमस्ते")){
            ans = "Namaste Boss, main Alina hoon. Bataiye, main aapki kya madad karun?";
        } else if(s.contains("naam") || s.contains("name")){
            ans = "Boss, mera naam Alina AI hai.";
        } else {
            ans = "Boss, aapne kaha: " + q;
        }

        response.setText(ans);
        setState("speaking");
        if(tts != null) tts.speak(ans, TextToSpeech.QUEUE_FLUSH, null, "alina");
        handler.postDelayed(this::stopSpeaking, Math.max(1800, ans.length()*65L));
    }

    void open(String pkg,String fallback){ try{startActivity(getPackageManager().getLaunchIntentForPackage(pkg));}catch(Exception e){openWeb(fallback);} }
    void openWeb(String url){ try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));}catch(Exception e){} }

    @Override protected void onDestroy(){ speaking=false; if(lipLoop!=null)handler.removeCallbacks(lipLoop); if(tts!=null){tts.stop();tts.shutdown();} super.onDestroy(); }
}