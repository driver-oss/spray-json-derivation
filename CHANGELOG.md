# Version 0.4.2

Build and publish for ScalaJS and Scala Native. This release adds a
dependency on a binary compatible fork of spray-json.

# Version 0.4.1

Add MiMa plugin to check binary backwards compatibility.

# Version 0.4.0

Same as version 0.3.1; the package rename should have warranted a
major version bump.

# Version 0.3.1

- Rename packages to "spray.json" for seamless integration with
  spray-json.
- Add companion object back to `DerivedFormats`.

# Version 0.3.0

Don't make derived formats implicitly available by default. This
copies functionality from `DerivedFormats` to `ImplicitDerivedFormats`
and removes the implicit qualifier from `DerivedFormats` generation.

# Version 0.2.2

Fix Scaladoc markup for warning-free builds.

# Version 0.2.1

Build for Scala 2.11 as well as 2.12.

# Version 0.2.0

Generate RootJsonFormats instead of JsonFormats.

# Version 0.1.1

Fix publish process through Travis CI.

# Version 0.1

Initial release.
