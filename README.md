# SyncIt

Morphe patches for **Sync for Reddit** (`com.laurencedawson.reddit_sync`).

Sync was discontinued after Reddit's 2023 API changes. This bundle fixes parts of its
behaviour that still matter when the app runs today.

## Add to Morphe

1. Install Morphe Manager and switch it to advanced/expert mode.
2. Add this patch source:
   <https://morphe.software/add-source?github=OgunHva/SyncIt>
3. Select the Sync for Reddit APK you want to patch.

**Supported app:** Sync for Reddit **v23.06.30-13:39** (free version,
`com.laurencedawson.reddit_sync`). Only this build is targeted.

## Patches

| Patch | Description |
|---|---|
| **Keep links in post previews** | Keeps links in the text under a post tappable in feed and card previews, where Sync otherwise drops every span it built. Images are still left out of previews, so feeds stay as light as before. |

## Building

Requirements: JDK 21 and Android SDK platform 36. Point `ANDROID_HOME` at the SDK
(or create `local.properties` with `sdk.dir=...`).

```sh
./gradlew :patches:buildAndroid
```

The patch bundle is written to `patches/build/libs/patches-<version>.mpp`.

## Notes

- The patched APK is signed with the patcher's key, so uninstall the original app before
  installing. The output is APK Signature Scheme v2 only, which means Android 7.0 or newer.
- The patch restores text spans (links, bold, italic) in previews. Image spans are still
  released, so previews do not start loading pictures they did not load before.

## Credits and license

The preview-span approach is based on
[Patcheddit](https://github.com/wchill/patcheddit) and
[Sync Up](https://github.com/RashKash103/sync-up), both GPL-3.0.

SyncIt is licensed under the [GNU General Public License v3.0](LICENSE). See
[NOTICE](NOTICE) for additional conditions inherited from Morphe.
