# Free course scorecards — GolfCourseAPI.com

This build adds course/club search, tee selection, hole pars and scorecard lengths,
and offline saved tee scorecards. It uses the provider's **free plan** (advertised
as 35 API requests per day per account on 8 October 2026). No server or paid hosting
is needed: each device owner supplies their own free provider account/API key.
No shared or build-time key is included in the public repository or APK.

## Activate on the phone

1. Open https://golfcourseapi.com/ and create/activate a free account.
2. Obtain the API key from your provider account. Do not paste it into GitHub,
   a commit, an issue, or this chat.
3. In the updated app, open **More → Course → Add free API key** and enter the
   key there. The field is masked. Save it, enter a club/course name and tap Search.
4. Tap a search result, choose the correct tee, and confirm a new scorecard if
   the current round already has scores. The selected tee is saved on the device.
5. Saved tees remain selectable offline even after removing the API key.

## Free-plan behaviour and limitations

- A search and fetching a course's tees are separate API requests. Search is
  button-driven, not triggered on every keystroke. There are no automatic retries,
  bulk downloads or subscription upgrades.
- Successful search/detail responses are cached for 24 hours. The optional
  **Refresh cached data** checkbox bypasses that cache and uses provider requests.
- Your account's daily allowance is shared with any other device using that
  account. On HTTP 429, the app explains the limit and keeps saved courses usable.
- Provider availability and country/course coverage are not guaranteed.
- This API supplies **scorecards, not hole GPS, green coordinates, hazard
  positions or distance-to-pin measurements**. Course-location coordinates must
  never be treated as green positions. The app does not do that.
- Provider metres are used when present; otherwise yards are converted using
  0.9144 metres/yard. Original yardages are retained for the Live Hole display.
- Incomplete tee data is rejected instead of inventing hole distances/pars.
- Existing manually entered/legacy course data is retained for existing rounds
  and labelled as not API-verified. Selecting a different tee resets the current
  scorecard only after confirmation when scores have already been entered.

## Key handling

The personal API key is encrypted using an AES-GCM key in Android Keystore.
Only ciphertext and its IV are stored in `noBackupFilesDir`; the key entry is not
saved in Compose instance state or displayed again after saving. It is sent only
in an Authorization header to the fixed HTTPS origin `api.golfcourseapi.com`.
HTTP redirects are disabled, and keys are not put into URLs, logs or source.

This is a per-device/personal-account integration, not a way to ship a shared
commercial API key. Rooted or compromised devices remain a risk. If this becomes
a publicly distributed app using a shared provider account, move that shared
credential to an authenticated server-side proxy instead.

## Build and verification

```sh
./gradlew testDebugUnitTest assembleDebug
```

Unit tests use synthetic JSON fixtures, not real course data or a live API key.
The existing GitHub Actions workflow runs on main and supports manual dispatch
for this feature branch. In CI, debug APK assembly also runs the unit tests
(configured in `app/build.gradle.kts`); no workflow-file edit or provider access
is required. Pull requests do not start that existing workflow automatically.
Live API authentication, course coverage, Android Keystore and offline behaviour
still need an on-device check with the owner's free account.

The existing application ID is preserved and the version code is incremented.
**Do not uninstall the current app merely to install a test APK.** Android also
requires the same signing certificate for an update. This repository's existing
workflow builds a debug APK without a persistent signing key, so update signing
compatibility with your installed copy is not verified. Uninstalling can erase
your on-device scores and settings; resolve signing/backup before replacing it.

Provider docs: https://api.golfcourseapi.com/docs/api
OpenAPI contract: https://api.golfcourseapi.com/docs/api/openapi.yml
