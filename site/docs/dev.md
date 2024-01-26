# Developing ApoML

There are two ways to develop ApoML:

1. locally
2. in the supplied virtual machine.

## Locally building and running ApoML

The Gradle wrapper script can do most of the work of
setting up your development environment.

```console
$ git clone github.com/ajrouvoet/apoML.kt
$ ./gradlew run --args "./examples/double.apo"
... gradle downloads jvm
... gradle downloads dependencies
... gradle builds ApoML
... gradle runs ApoML main with the supplied args
```

This however merges the stdout from ApoML and Gradle,
which may be confusing. The following works better:

```console
$ ./gradlew install
$ ./apoML/build/install/bin/apoml ./examples/double.apo
```

## Using the supplied VM

The local methods of developing ApoML requires a compatible
system environment. A Linux VM is available that has everything
you need, including the prepared repository.