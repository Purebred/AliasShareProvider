# AliasShareProvider

Purebred provisions keys on Android devices such that applications can use the keys via the Android [Key Chain API](https://developer.android.com/reference/android/security/KeyChain). User confirmation is required before an app can use a key via the KeyChain API. The choosePrivateKey alias method is used to solicit confirmation. An alias can be passed to choosePrivateKey to preselect that value in the dialog box presented to the user. However, the KeyChain provides no means to enumerate aliases or certificates that are available via the KeyChain to facilitate passing an alias to choosePrivateKey. 

Recent versions of the Purebred Registration app features a content provider that can be used by applications desiring to use Purebred-provisioned keys via the KeyChain API. The content provider allows applications to obtain a list of aliases and certificates that were provisioned via Purebred to enable a better user experience when using choosePrivateKey.

AliasShareProvider provides an alternative implementation of the content provider implemented by the Purebred application to spare the need to interact with a portal instance during testing. Additionally, AliasShareProvider provides a means of simulating broadcast events emitted by the Purebred Registration app. 
