<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# CorDapp Template with Sampler

Welcome to the Kotlin CorDapp with Sampler template. The CorDapp template is extended from [CorDapp Template - Kotlin](https://github.com/corda/cordapp-template-kotlin) to demonstrate how to integrate a sampler module into your CorDapp project. 

# Pre-Requisites

## Corda Performance Test Suite
The development of CorDapp and sampler itself does not require Corda Performance Test Suite, but you will need it to create and execute test plans. If you are not familiar with Corda Performance Test Suite or how to implement JMeter sampler, please refer to the [Corda documentation](https://docs.corda.net/docs/corda-enterprise/4.8/performance-testing/toc-tree.html).


# Usage

## Build the sampler

You can build the sampler by simply execute build task in Gradle.

```

./gradlew jmeter-sampler:build

```
This will generate the output of the build under `jmeter-sampler/build/`, just like other CorDapp modules.

## Deploy the sampler
In `jmeter-sampler/build.gradle` a `deploySampler` task is defined. This task basically syncs up a deploy folder with the sampler and worflow jar files (as in most cases the sampler has dependencies on workflows of the CorDapp). By default, the deploy folder is `jmeter-sampler/extlibs/`. You can specify a different location by changing the value of `samplerDeployPath` property in [gradle.properties](https://github.com/davidleesbir3/cordapp-template-with-sampler/blob/with-sampler/gradle.properties) file. 

For example, if you have Test Suite installed in `~/Corda-Test-Suite/`, then you might want to set the deploy folder to `~/Corda-Test-Suite/extlibs`. Then run the following command to deploy the sampler:

```
./gradlew jmeter-sampler:deploySampler
```
or simply

```
./gradlew deploySampler
```

Once it's delpoyed successfully, you should see two jar files in the deploy folder:

* jmeter-sampler-1.0.jar
* workflows-0.1.jar

## Running Corda Performance Test Suite
Test Suite comes with a command line argument `-XadditionalSearchPaths` to specify paths for jar files containing classes that need to be loaded. For example, if your JMeter test plan will invoke a class in your sampler, you would need your sampler jar file to be loaded. Assuming Test Suite JAR file is installed in `~/Corda-Test-Suite/`, and sampler deploy folder is `~/Corda-Test-Suite/extlibs/`, you can start up Test Suite GUI by executing the following command

```
cd ~/Corda-Test-Suite/

java -jar jmeter-corda.jar -XadditionalSearchPaths="./extlibs/" -XjmeterProperties ./jmeter.properties
```

Performance Test Suite should be started with the sampler and its dependencies loaded. Note that you can specify multiple paths in the `-XadditionalSearchPaths` properties by separating them with semicolon.

For more information on available arguments of Peformance Test Suite, please refer to [Corda documentation](https://docs.corda.net/docs/corda-enterprise/4.8/performance-testing/running-jmeter-corda.html#jmeter-corda-wrapper-arguments).



