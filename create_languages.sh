#!/bin/bash
langs=("es" "fr" "de" "it" "pt" "ru" "zh-rCN" "ja" "ko" "ar" "hi" "bn" "tr" "vi" "th" "in" "nl" "pl" "sv" "el")
base_dir="app/src/main/res"
for lang in "${langs[@]}"; do
    mkdir -p "$base_dir/values-$lang"
    cat << 'XML_EOF' > "$base_dir/values-$lang/strings.xml"
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">DoseFlow</string>
</resources>
XML_EOF
done
echo "Created 20 language directories."
