# IDE Compatibility

The plugin targets IntelliJ Platform 2025.2 (build 252) and newer releases.

The plugin intentionally does not define an upper `until-build` limit. JetBrains recommends leaving `until-build` unset when a plugin is not published as a separate build for every major IDE release. This allows newer IntelliJ-based IDE releases to install the plugin while compatibility is verified by the Plugin Verifier and CI.

When an IntelliJ Platform API change requires a compatibility-specific implementation, the supported range should be narrowed only as needed.
