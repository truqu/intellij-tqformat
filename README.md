# intellij-tqformat

![Build](https://github.com/truqu/intellij-tqformat/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.truqu.intellijtqformat.svg)](https://plugins.jetbrains.com/plugin/com.truqu.intellijtqformat)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.truqu.intellijtqformat.svg)](https://plugins.jetbrains.com/plugin/com.truqu.intellijtqformat)

<!-- Plugin description -->
The `intellij-tqformat` plugin allows configuring [tqformat](https://github.com/truqu/tqformat) as an (external) code formatter for Erlang code.

In order to use, get the escript version of `tqformat`. Select a (compatible) Erlang SDK in IntelliJ, and select the correct path to the `tqformat` escript in your project settings.

Format-on-save is available as a setting as well.  
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "intellij-tqformat"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/truqu/intellij-tqformat/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
