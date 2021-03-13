Looking to report a Bug or make a feature request? Take a look [here](https://github.com/shadow578/tenshi#issues-feature-requests-and-contributing).

---

### Thank you for your interest in contributing to Tenshi!

⚠ This Page is under Construction ⚠


# Translations
Translations are currently only possible by directly editing the strings.xml file. 

 __I'm planning to use a Service like Crowdin or Weblate soon.__

# Code Contributions

__Pull requests are welcome!__
Please read follow the Code Style Guidelines below.

If you're interested in taking on [an open issue](https://github.com/shadow578/tenshi/issues), please comment on it so others are aware.

# Forks

Forks are allowed so long they abide by [Tenshi's LICENSE](LICENSE)

When creating a fork, remember to:

- Avoid confusion with the main app by:
    - Changing the app name (strings/shared_app_name)
    - Changing the app icon
    - Changing the OAUTH redirect URL
- To avoid conflicts while installing:
    - Change the 'applicationId' in build.gradle
- To allow login:
    - [Create a new API Client](https://myanimelist.net/apiconfig)
        - Please use a different redirect URL for your fork
    - Paste your CLIENT_ID into the Constant in Secrets.java
    - Update the redirect URL
        - in Urls.java
        - in AndroidManifest.xml


# Code Style Guidelines

These are the guidelines you should follow when contributing code to Tenshi.<br>
These Guidelines outline what I think are useful rules to create readable and manageable code, tho they are always open for discussion(i'm not a professional developer after all, so what do i know :P)

- Please use Java
- Do not hardcode random values (like intent extras, ...), but use constants in the appropriate classes instead
- Avoid hardcoding URLs, use constants in Urls.java instead
- Use Enums instead of string / integer constants (especially in MAL model)
    - Enum values are in PascalCase (i know, java default is all uppercase, but that look stupid)
    - Use @SerializedName if the enum name differs from eg. an API requirement
    - Use EnumHelper instead of Enum functions to allow for @SerializedName
- Write descriptive JavaDoc comments for:
    - __all__ classes, interface and enums
    - __all__ public fields and enum constants
    - __all__ public methods
    - _optionally_ private fields and methods
- Use @Nullable / @NonNull annotations on:
    - __all__ public fields and methods
    - _optionally_ private fields and methods
- Usage of Lambda expression for handlers and callbacks is __strongly encouraged__.
- Try to use [Tenshis language extensions](./LANG.md) instead of writing extra code.



