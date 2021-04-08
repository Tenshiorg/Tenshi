Looking to report a Bug or make a feature request? Take a look [here](https://github.com/Tenshiorg/Tenshi#issues-feature-requests-and-contributing).

---

### Thank you for your interest in contributing to Tenshi!

⚠ This Page is under Construction ⚠


# Translations
Translations are currently only possible by directly editing the strings.xml file. 

 __I'm planning to use a Service like Crowdin or Weblate soon.__

# Code Contributions

__Pull requests are welcome!__
Please read follow the Code Style Guidelines below.

If you're interested in taking on [an open issue](https://github.com/Tenshiorg/Tenshi/issues), please comment on it so others are aware.

# Forks

Forks are allowed so long they abide by [Tenshi's LICENSE](LICENSE)

When creating a fork, remember to:

- Avoid confusion with the main app and conflicts by:
    - Changing the app name (strings/shared_app_name)
    - Changing the app icon
    - Changing the OAUTH redirect URL
    - Change the 'applicationId' in build.gradle
- [Create a new API Client](https://myanimelist.net/apiconfig)
- Setup your build config (/app/build.properties)
    - copy the [sample config](https://github.com/Tenshiorg/Tenshi/blob/kohai/app/build.properties.sample) to /app/build.properties and fill the values as per the comments
    - ⚠ Make sure the build config is __NOT__ tracked by git
 
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
- Have a look at [Tenshis' Language Extensions](https://github.com/Tenshiorg/Extensions-Lib/blob/kohai/LANG.md).
