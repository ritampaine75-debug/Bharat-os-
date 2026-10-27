package com.example.util

import com.example.data.model.LauncherLanguage

object Localization {

    fun getGreeting(lang: LauncherLanguage, hour: Int): String {
        return when (lang) {
            LauncherLanguage.HINDI -> when (hour) {
                in 4..11 -> "सुप्रभात"
                in 12..16 -> "शुभ दोपहर"
                in 17..21 -> "शुभ संध्या"
                else -> "शुभ रात्रि"
            }
            LauncherLanguage.BENGALI -> when (hour) {
                in 4..11 -> "শুভ সকাল"
                in 12..16 -> "শুভ দুপুর"
                in 17..21 -> "শুভ সন্ধ্যা"
                else -> "শুভ রাত্রি"
            }
            LauncherLanguage.ENGLISH -> when (hour) {
                in 4..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                in 17..21 -> "Good Evening"
                else -> "Good Night"
            }
        }
    }

    fun getText(key: String, lang: LauncherLanguage): String {
        return when (lang) {
            LauncherLanguage.HINDI -> HINDI_STRINGS[key] ?: ENGLISH_STRINGS[key] ?: key
            LauncherLanguage.BENGALI -> BENGALI_STRINGS[key] ?: ENGLISH_STRINGS[key] ?: key
            LauncherLanguage.ENGLISH -> ENGLISH_STRINGS[key] ?: key
        }
    }

    private val ENGLISH_STRINGS = mapOf(
        "app_title" to "Bharat OS",
        "search_hint" to "Search installed apps…",
        "all_apps" to "All Apps",
        "favorites" to "Favorites",
        "settings" to "Settings",
        "wallpapers" to "Wallpapers",
        "get_started" to "Get Started",
        "welcome_header" to "Namaste, Welcome to Bharat OS",
        "welcome_desc" to "A functional, dignified Android launcher crafted with Indian design sensibilities.",
        "set_default" to "Set as Default Launcher",
        "default_active" to "Bharat OS is your Default Launcher",
        "default_inactive" to "Not set as default launcher",
        "tap_to_set_default" to "Tap to select Bharat OS as your primary home screen.",
        "skip" to "Continue to Home",
        "grid_size" to "Grid Density",
        "icon_size" to "Icon Scale",
        "show_labels" to "Show App Names",
        "theme" to "Visual Theme",
        "language" to "System Language",
        "dock_settings" to "Dock Favorites",
        "reset_layout" to "Reset Home Layout",
        "reset_confirm" to "Reset layout to default shortcuts?",
        "reset" to "Reset",
        "cancel" to "Cancel",
        "privacy_title" to "Privacy & Permissions",
        "privacy_desc" to "Bharat OS processes all app indexing on-device. No telemetry, analytics, or background tracking are collected.",
        "perm_query_packages" to "Package Visibility",
        "perm_query_packages_desc" to "Required by Android 11+ to discover and launch your installed applications.",
        "perm_mic" to "Microphone (Optional)",
        "perm_mic_desc" to "Used only when triggering voice search from the home search bar.",
        "perm_storage" to "Photo Picker (Optional)",
        "perm_storage_desc" to "Used when selecting custom gallery images as home wallpaper.",
        "about" to "About Bharat OS",
        "about_version" to "Version 1.0 (Production)",
        "about_desc" to "Designed for Android devices across India with native performance, zero bloat, and respectful privacy.",
        "app_info" to "App Details",
        "uninstall" to "Uninstall",
        "add_to_home" to "Add to Home Screen",
        "remove_from_home" to "Remove from Home",
        "pin_to_dock" to "Pin to Dock",
        "unpin_dock" to "Remove from Dock",
        "favorite" to "Add to Favorites",
        "unfavorite" to "Remove from Favorites",
        "launch_failed" to "Unable to launch this application",
        "custom_wallpaper" to "Choose from Device Photos",
        "voice_search" to "Voice Search",
        "empty_apps" to "No applications matching search",
        "role_rationale" to "To become your primary home screen, Android requires launcher role authorization. You can change this at any time in Android System Settings."
    )

    private val HINDI_STRINGS = mapOf(
        "app_title" to "भारत ओएस",
        "search_hint" to "ऐप्स खोजें…",
        "all_apps" to "सभी ऐप्स",
        "favorites" to "पसंदीदा",
        "settings" to "सेटिंग्स",
        "wallpapers" to "वॉलपेपर",
        "get_started" to "आरंभ करें",
        "welcome_header" to "नमस्ते, भारत ओएस में आपका स्वागत है",
        "welcome_desc" to "भारतीय सांस्कृतिक गरिमा और सुरुचिपूर्ण डिज़ाइन के साथ तैयार किया गया एंड्रॉइड लॉन्चर।",
        "set_default" to "डिफ़ॉल्ट लॉन्चर सेट करें",
        "default_active" to "भारत ओएस आपका मुख्य लॉन्चर है",
        "default_inactive" to "डिफ़ॉल्ट लॉन्चर के रूप में सेट नहीं है",
        "tap_to_set_default" to "भारत ओएस को अपनी प्राथमिक होम स्क्रीन बनाने के लिए टैप करें।",
        "skip" to "होम स्क्रीन पर जाएं",
        "grid_size" to "ग्रिड आकार",
        "icon_size" to "आइकन का आकार",
        "show_labels" to "ऐप के नाम दिखाएं",
        "theme" to "थीम और दिखावट",
        "language" to "भाषा (Language)",
        "dock_settings" to "डॉक पसंदीदा",
        "reset_layout" to "होम लेआउट रीसेट करें",
        "reset_confirm" to "क्या आप डिफ़ॉल्ट शॉर्टकट पुनः स्थापित करना चाहते हैं?",
        "reset" to "रीसेट करें",
        "cancel" to "रद्द करें",
        "privacy_title" to "गोपनीयता और अनुमतियाँ",
        "privacy_desc" to "भारत ओएस सभी जानकारी डिवाइस पर ही रखता है। कोई भी बाहरी ट्रैकिंग या डेटा संग्रह नहीं किया जाता।",
        "perm_query_packages" to "ऐप दृश्यता",
        "perm_query_packages_desc" to "इंस्टॉल किए गए ऐप्स को खोजने और खोलने के लिए आवश्यक।",
        "perm_mic" to "माइक्रोफ़ोन (वैकल्पिक)",
        "perm_mic_desc" to "केवल सर्च बार में आवाज़ से खोजने पर उपयोग होता है।",
        "perm_storage" to "फ़ोटो चयन (वैकल्पिक)",
        "perm_storage_desc" to "गैलरी से कस्टम वॉलपेपर चुनते समय उपयोग किया जाता है।",
        "about" to "भारत ओएस के बारे में",
        "about_version" to "संस्करण 1.0 (प्रोडक्शन)",
        "about_desc" to "भारतीय उपयोगकर्ताओं के लिए शुद्ध प्रदर्शन और पूर्ण गोपनीयता के साथ निर्मित।",
        "app_info" to "ऐप विवरण",
        "uninstall" to "अनइंस्टॉल करें",
        "add_to_home" to "होम स्क्रीन पर जोड़ें",
        "remove_from_home" to "होम से हटाएं",
        "pin_to_dock" to "डॉक में जोड़ें",
        "unpin_dock" to "डॉक से हटाएं",
        "favorite" to "पसंदीदा में जोड़ें",
        "unfavorite" to "पसंदीदा से हटाएं",
        "launch_failed" to "ऐप शुरू करने में असमर्थ",
        "custom_wallpaper" to "फ़ोन गैलरी से चुनें",
        "voice_search" to "आवाज़ खोज",
        "empty_apps" to "कोई ऐप नहीं मिला",
        "role_rationale" to "प्राथमिक होम स्क्रीन बनने के लिए एंड्रॉइड लॉन्चर अनुमति की आवश्यकता होती है। आप इसे कभी भी बदल सकते हैं।"
    )

    private val BENGALI_STRINGS = mapOf(
        "app_title" to "ভারত ওএস",
        "search_hint" to "অ্যাপ সন্ধান করুন…",
        "all_apps" to "সমস্ত অ্যাপ",
        "favorites" to "প্রিয়",
        "settings" to "সেটিংস",
        "wallpapers" to "ওয়ালপেপার",
        "get_started" to "শুরু করুন",
        "welcome_header" to "নমস্কার, ভারত ওএস-এ স্বাগতম",
        "welcome_desc" to "ভারতীয় নান্দনিকতা ও পরিশীলিত ডিজাইনে তৈরি আধুনিক অ্যান্ড্রয়েড লঞ্চার।",
        "set_default" to "ডিফল্ট লঞ্চার সেট করুন",
        "default_active" to "ভারত ওএস আপনার ডিফল্ট লঞ্চার",
        "default_inactive" to "ডিফল্ট লঞ্চার হিসেবে নির্ধারিত নয়",
        "tap_to_set_default" to "ভারত ওএস-কে প্রধান হোম স্ক্রিন করতে ট্যাপ করুন।",
        "skip" to "হোম স্ক্রিনে যান",
        "grid_size" to "গ্রিড আকার",
        "icon_size" to "আইকন আকার",
        "show_labels" to "অ্যাপ নাম দেখান",
        "theme" to "থিম",
        "language" to "ভাষা (Language)",
        "dock_settings" to "ডক পছন্দ",
        "reset_layout" to "হোম লেআউট রিসেট",
        "reset_confirm" to "আপনি কি ডিফল্ট লেআউট পুনরুদ্ধার করতে চান?",
        "reset" to "রিসেট",
        "cancel" to "বাতিল",
        "privacy_title" to "গোপনীয়তা ও অনুমতি",
        "privacy_desc" to "ভারত ওএস সমস্ত তথ্য আপনার ডিভাইসে রাখে। কোনও ট্র্যাকিং বা তথ্য সংগ্রহ হয় না।",
        "perm_query_packages" to "প্যাকেজ দৃশ্যমানতা",
        "perm_query_packages_desc" to "ইনস্টল করা অ্যাপ খুঁজে বের করতে ও খুলতে ব্যবহৃত।",
        "perm_mic" to "মাইক্রোফোন (ঐচ্ছিক)",
        "perm_mic_desc" to "ভয়েস অনুসন্ধানের সময় ব্যবহৃত হয়।",
        "perm_storage" to "ফটো পিকার (ঐচ্ছিক)",
        "perm_storage_desc" to "কাস্টম ওয়ালপেপার নির্বাচনের জন্য ব্যবহৃত।",
        "about" to "ভারত ওএস সম্পর্কে",
        "about_version" to "সংস্করণ ১.০",
        "about_desc" to "উচ্চ কার্যক্ষমতা ও গোপনীয়তার সঙ্গে ভারতীয় ব্যবহারকারীদের জন্য নির্মিত।",
        "app_info" to "অ্যাপ বিবরণ",
        "uninstall" to "আনইনস্টল করুন",
        "add_to_home" to "হোমে যোগ করুন",
        "remove_from_home" to "হোম থেকে মুছুন",
        "pin_to_dock" to "ডকে রাখুন",
        "unpin_dock" to "ডক থেকে সরান",
        "favorite" to "প্রিয়তে যোগ করুন",
        "unfavorite" to "প্রিয় থেকে সরান",
        "launch_failed" to "অ্যাপ শুরু করা যায়নি",
        "custom_wallpaper" to "গ্যালারি থেকে বাছুন",
        "voice_search" to "ভয়েস অনুসন্ধান",
        "empty_apps" to "কোনও মিল পাওয়া যায়নি",
        "role_rationale" to "মূল হোম স্ক্রিন হিসেবে কাজ করার জন্য সিস্টেম অনুমতি প্রয়োজন। এটি যেকোনো সময় পরিবর্তনযোগ্য।"
    )
}
