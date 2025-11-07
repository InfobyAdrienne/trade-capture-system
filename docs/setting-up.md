### Issue: JDK 25 Too Modern for This Project

While setting up the project, I encountered an issue caused by using JDK 25, which is not yet supported by some dependencies. The project requires JDK 21 instead.

### Steps to Fix

1. Install or switch to JDK 21
Make sure JDK 21 is installed (e.g. via Homebrew)

2. Update your shell configuration
Edit your .zshrc to point to JDK 21:

`export JAVA_HOME=$(/usr/libexec/java_home -v 21)`

`export PATH=$JAVA_HOME/bin:$PATH`


Then reload your shell:

`source ~/.zshrc`


### Verify your setup

Check that your environment points to the correct JDK:

`echo $JAVA_HOME`

`java -version`

The output should show JDK 21.

Ensure the correct JDK is first in your PATH
Your $PATH should list the JDK 21 binaries before any others.

### Change configuaration in VS Code

1. Open the Command Palette (Cmd+Shift+P)

2. Search for “Configure Java Runtime”

3. Set JDK 21 as the runtime for your project.

### Update Maven configuration

Ensure your pom.xml specifies the correct compiler version:

`<maven.compiler.source>21</maven.compiler.source>`
`<maven.compiler.target>21</maven.compiler.target>`