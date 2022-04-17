# Shruum
A Monerujo fork without the bloat.

### QUICKSTART
- Download the APK for the most current release [here](https://mayumi.one) and install it
- Run the App and select "Generate Wallet" to create a new wallet or recover a wallet
- Advanced users can copy over synced wallet files (all files) onto sdcard in directory Monerujo (created first time App is started)
- See the [FAQ](doc/FAQ.md)

### Random Notes
- works on the mainnet & stagenet
- use your own daemon - it's easy
- Monerujo means "Monero Wallet" according to https://www.reddit.com/r/Monero/comments/3exy7t/esperanto_corner/

### Issues / Pitfalls
- Users of Zenfone MAX & Zenfone 2 Laser (possibly others) **MUST** use the armeabi-v7a APK as the arm64-v8a build uses hardware AES
functionality these models don't have.
- You should backup your wallet files in the "monerujo" folder periodically.
- Also note, that on some devices the backups will only be visible on a PC over USB after a reboot of the device (it's an Android bug/feature)
- Created wallets on a private testnet are unusable because the restore height is set to that
of the "real" testnet.  After creating a new wallet, make a **new** one by recovering from the seed.
The official monero client shows the same behaviour.

### HOW TO BUILD

See [the instructions](doc/BUILDING-external-libs.md)

Then, fire up Android Studio and build the APK.

### Donations
- Address: 833EdLq5DwghPkQZTjXhYJg2RQBpwCAf7UGyt78CjxnffKHF3uwaeYyijbPwAGPWt7GCpHVbaM9DE29FZtYoHc4B4LssmQj
