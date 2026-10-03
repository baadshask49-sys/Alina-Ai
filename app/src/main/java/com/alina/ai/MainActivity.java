
void reply(String q) {
    setState("thinking");

    String s = q.toLowerCase(Locale.ROOT).trim();
    String ans;

    if (s.contains("youtube")) {
        open("com.google.android.youtube", "https://www.youtube.com");
        ans = "Boss, YouTube khol rahi hoon.";

    } else if (s.contains("whatsapp")) {
        open("com.whatsapp", "https://www.whatsapp.com");
        ans = "Boss, WhatsApp khol rahi hoon.";

    } else if (s.contains("instagram")) {
        open("com.instagram.android", "https://www.instagram.com");
        ans = "Boss, Instagram khol rahi hoon.";

    } else if (s.contains("facebook")) {
        open("com.facebook.katana", "https://www.facebook.com");
        ans = "Boss, Facebook khol rahi hoon.";

    } else if (s.contains("chrome")) {
        open("com.android.chrome", "https://www.google.com");
        ans = "Boss, Chrome khol rahi hoon.";

    } else if (s.contains("camera") || s.contains("कैमरा")) {
        try {
            startActivity(new Intent("android.media.action.IMAGE_CAPTURE"));
            ans = "Boss, camera khol rahi hoon.";
        } catch (Exception e) {
            ans = "Boss, camera nahi khul paaya.";
        }

    } else if (s.contains("gallery") || s.contains("photos")
            || s.contains("गैलरी")) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setType("image/*");
            startActivity(i);
            ans = "Boss, gallery khol rahi hoon.";
        } catch (Exception e) {
            ans = "Boss, gallery nahi khul paayi.";
        }

    } else if (s.contains("settings") || s.contains("सेटिंग")) {
        try {
            startActivity(new Intent(
                android.provider.Settings.ACTION_SETTINGS));
            ans = "Boss, settings khol rahi hoon.";
        } catch (Exception e) {
            ans = "Boss, settings nahi khul paayi.";
        }

    } else if (s.contains("phone") || s.contains("dialer")
            || s.contains("फोन")) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL));
            ans = "Boss, phone khol rahi hoon.";
        } catch (Exception e) {
            ans = "Boss, phone nahi khul paaya.";
        }

    } else if (s.contains("apps") || s.contains("all apps")
            || s.contains("saare app") || s.contains("sabhi app")) {
        showInstalledApps();
        ans = "Boss, installed apps ki list dikha rahi hoon.";

    } else if (s.contains("hello") || s.equals("hi")
            || s.contains("हाय") || s.contains("नमस्ते")) {
        ans = "Namaste Boss, main Alina hoon. Bataiye, main aapki kya madad karun?";

    } else if (s.contains("naam") || s.contains("name")) {
        ans = "Boss, mera naam Alina AI hai.";

    } else {
        String appName = s
            .replace("please", "")
            .replace("open", "")
            .replace("kholo", "")
            .replace("khol do", "")
            .replace("chalao", "")
            .replace("app", "")
            .trim();

        if (!appName.isEmpty() && tryOpenAppByName(appName)) {
            ans = "Boss, app kholne ki koshish kar rahi hoon.";
        } else {
            ans = "Boss, main samajh nahi paayi. App ka naam dobara boliye.";
        }
    }

    response.setText(ans);
    setState("speaking");

    if (tts != null) {
        tts.speak(ans, TextToSpeech.QUEUE_FLUSH, null, "alina");
    }

    handler.postDelayed(
        this::stopSpeaking,
        Math.max(1800, ans.length() * 65L)
    );
}

boolean tryOpenAppByName(String query) {
    try {
        android.content.pm.PackageManager pm = getPackageManager();
        List<android.content.pm.ApplicationInfo> apps =
            pm.getInstalledApplications(0);

        for (android.content.pm.ApplicationInfo app : apps) {
            String label = pm.getApplicationLabel(app)
                .toString().toLowerCase(Locale.ROOT);

            if (label.equals(query) || label.contains(query)) {
                Intent launch = pm.getLaunchIntentForPackage(app.packageName);

                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launch);
                    return true;
                }
            }
        }
    } catch (Exception e) {
        return false;
    }

    return false;
}

void showInstalledApps() {
    android.content.pm.PackageManager pm = getPackageManager();
    List<android.content.pm.ApplicationInfo> apps =
        pm.getInstalledApplications(0);

    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> packages = new ArrayList<>();

    for (android.content.pm.ApplicationInfo app : apps) {
        Intent launch = pm.getLaunchIntentForPackage(app.packageName);
        if (launch != null) {
            names.add(pm.getApplicationLabel(app).toString());
            packages.add(app.packageName);
        }
    }

    Collections.sort(names, String.CASE_INSENSITIVE_ORDER);

    new AlertDialog.Builder(this)
        .setTitle("Boss, installed apps")
        .setItems(names.toArray(new String[0]), (dialog, which) -> {
            String selected = names.get(which);
            for (android.content.pm.ApplicationInfo app : apps) {
                String label = pm.getApplicationLabel(app).toString();
                if (label.equals(selected)) {
                    Intent launch =
                        pm.getLaunchIntentForPackage(app.packageName);
                    if (launch != null) startActivity(launch);
                    break;
                }
            }
        })
        .setNegativeButton("Close", null)
        .show();
}
