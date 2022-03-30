#How to  Contribute

You can contribute in different ways:

* pick up an issue and do the coding, preferably bugs over features
* increase the test coverage of Auditor itself (but only for real logic)
* improve the documentation
* report bugs
* suggest new features and improvements
* spread the word

### Before submitting an issue

- If you're not using the latest master to generate API clients or server stubs, please give it another try by pulling the latest master as the issue may have already been addressed. Ref: [Getting Started](https://github.com/swagger-api/swagger-codegen#getting-started)
- Search the [open issue](https://github.com/lowes/auditor/issues/) and [closed issue](https://github.com/lowes/auditor/issues?q=is%3Aissue+is%3Aclosed) to ensure no one else has reported something similar before.
- File an [issue](https://github.com/lowes/auditor/issues/new) by providing all the required information.
- Test with the latest master by building the JAR locally to see if the issue has already been addressed.
- You can also make a suggestion or ask a question by opening an "issue".

### Setup
All commands mentioned below are expected to be run from your project root location.

After cloning the repository in your local, you need to run the following command to install `ktlintFormat` git pre-commit hook:
```
./gradlew addKtlintFormatGitPreCommitHook
```
This ensures the files are formatted before checking in.

#### Build and test
To build the project, you need to run the following:
```
 ./gradlew clean build
``` 
This will build and test all the modules(core, client, app, etc).
If you need to run test specifically, run following commands as per your needs:
```
./gradlew clean test -> Runs all unit tests
./gradlew clean integrationTest -> Runs all integrataion tests
./gradlew clean functionalTest -> Runs all functional tests
```

#### Adding/Updating dependencies
We are using [refreshVersions](https://github.com/jmfayard/refreshVersions) to manage version upgrades.
When you add a new dependency, please run the following:
```
./gradlew refreshVersionsMigrate
```
This will migrate the versions to [versions.properties](./versions.properties).
To upgrade a specific dependency or all dependencies, run the following:
```
./gradlew refreshVersions
```
Above command will fetch the latest versions of all dependencies used in the project.
you should pick the appropriate version to upgrade to. It can be done by copying the given version under comments and putting it against the said dependency property key.

### Coding conventions

We follow the [Kotlin Coding Conventions](http://kotlinlang.org/docs/reference/coding-conventions.html).

* Minimize mutability
* Choose self-explanatory names

You may find the current code base not 100% conform to the coding style and we welcome contributions to fix those.

### Before submitting a PR

- Search the [open issue](https://github.com/lowes/auditor/issues/) to ensure no one else has reported something similar and no one is actively working on similar proposed change.
- If no one has suggested something similar, open an ["issue"](https://github.com/lowes/auditor/issues/new) with your suggestion to gather feedback from the community.
- It's recommended to fork the lowes/auditor repo & **create a new git branch** for the change so that the merge commit message looks nicer in the commit history.
- We follow [Conventional Commits Specification](https://www.conventionalcommits.org/en/v1.0.0/#summary).
- The commit message should be structured as follows:
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]

[optional BREAKING CHANGE: <desctiption about breaking changes, if any>]
```

### Tips
- Smaller changes are easier to review
- Add test case(s) to cover the change
- Document the fix in the code to make the code more readable
- Make sure test cases passed after the change
- File a PR with meaningful title, description and commit messages.
- To close an issue (e.g. issue 123) automatically after a PR is merged, use keywords "fix", "close", "resolve" in the PR description, e.g. `closes #123`. (Ref: [closing issues using keywords](https://help.github.com/articles/closing-issues-using-keywords/))