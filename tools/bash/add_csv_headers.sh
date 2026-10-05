#!/usr/bin/env bash
set -euo pipefail

DIR="/root/resources/SymbolsData/Portfolio"
HEADER="<DATE>,<TIME>,<OPEN>,<HIGH>,<LOW>,<CLOSE>,<VOL>"

for f in "$DIR"/*; do
    [ -f "$f" ] || continue
    case "$(basename "$f")" in
        .*|_*) continue ;;
    esac

    first=$(head -n 1 "$f")
    if [[ "$first" == \<DATE\>* ]]; then
        echo "skip (has header): $f"
    else
        tmp=$(mktemp "${f}.tmp.XXXXXX")
        printf '%s\n' "$HEADER" > "$tmp"
        cat "$f" >> "$tmp"
        chmod --reference="$f" "$tmp"
        cat "$tmp" > "$f"
        rm "$tmp"
        echo "added header: $f"
    fi

    base=$(basename "$f")
    if [[ "$base" == DAT_MT_*_*_*.csv ]]; then
        rest=${base#DAT_MT_}
        symbol=${rest%%_*}
        rest2=${rest#*_}
        tf=${rest2%%_*}
        new="${symbol}_${tf}.csv"
        mv "$f" "$DIR/$new"
        echo "renamed: $base -> $new"
    fi
done
