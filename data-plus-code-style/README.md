# marathon-docker-template for "data" plus "code" contests
Template submission format for participating in Topcoder Marathon Matches.
Information in the challenge specification always overrides information in this document.

## Submission format
This template supports contests that require the combination of "submit data" and "submit code" submission styles. Typically this means that data submissions are evaluated during the provisional testing phase, and code is executed during final testing.

Your submission should be a single ZIP file with the following content:

```
/solution
   solution.csv
/code
   Dockerfile
   flags.txt // optional
   <your code>
```

The file `/solution/solution.csv` is the output your algorithm generates on the provisional test set. The format of this file will be described in the challenge specification.

The `/code` directory should contain a dockerized version of your system that will be used to reproduce your results in a well defined, standardized way. This folder must contain a `Dockerfile` that will be used to build a docker container that will host your system during final testing. How you organize the rest of the contents of the `/code` folder is up to you, as long as it satisfies the requirements listed below in the Final testing section. This repository contains code created in a toy challenge, for demonstration only. See the [Sample challenge](#a-sample-challenge) section at the end of this document for details.

#### Notes:
- During provisional testing only your `solution.csv` file will be used for scoring, however the tester tool will verify that your submission file conforms to the required format. This means that at least the `/code/Dockerfile` must be present from day 1, even if it doesn't describe any meaningful system to be built. However, we recommend that you keep working on the dockerized version of your code as the challenge progresses, especially if you are at or close to a prize winning rank on the provisional leaderboard.

- Make sure that your submission package is smaller than 500 MB. This means that if you use large files (external libraries, data files, pretained model files, etc) that won't fit into this limit, then your docker build process must download these from the net during building. There are several ways to achieve this, e.g. external libraries may be installed from a git repository, data files may be downloaded using `wget` or `curl` from Dropbox or Google Drive or any other public file hosting service. In any case always make sure that your build process is carefully tested end to end before you submit your package for final testing.

- During final testing your last submission file will be used to build your docker container.

- Make sure that the contents of the `/solution` and `/code` folders are in sync, i.e. your solution.csv file contains the exact output of the current version of your code.

## Final testing

To be able to successfully submit your system for final testing, some familiarity with [Docker](https://www.docker.com/) is required. If you have not used this technology before then you may first check [this page](https://www.docker.com/why-docker) and other learning material linked from there. To install Docker follow [these instructions](https://www.docker.com/community-edition).

In some contest you will work with GPU-accelerated systems in which case Nvidia-docker will also be required. See how to install Nvidia-docker [here](https://github.com/NVIDIA/nvidia-docker). Note that all sample `docker` commands given below should be replaced with `nvidia-docker` in this case.

## Contents of the /code folder
The `/code` folder of your submission must contain:
1. All your code (training and inference) that are needed to reproduce your results.
2. A dockerfile (named `Dockerfile`, without extension) that will be used to build your system.
3. All data files that are needed during training and inference, with the exception of
    - the contest's own training and testing data. You may assume that the training and testing data (as described in the problem statement's "Input files" section) will be available on the machine where your docker container runs, compressed files already unpacked,
    - large data files that can be downloaded automatically either during building or running your docker script.
4. Your trained model file(s). Alternatively your build process may download your model files from the network. Either way, you must make it possible to run inference without having to execute training first.

The tester tool will unpack your submission, and the
```
docker build -t <id> .
```
command will be used to build your docker image (the final '.' is significant), where `<id>` is your TopCoder handle.

The build process must run out of the box, i.e. it should download and install all necessary 3rd party dependencies, either download from internet or copy from the unpacked submission all necessary external data files, your model files, etc.
Your container will be started by the
```
docker run -v <local_data_path>:/data:ro -v <local_writable_area_path>:/wdata -it <id>
```
command, where the `-v` parameter mounts the contest's data to the container's `/data` folder. This means that all the raw contest data will be available for your container within the `/data` folder. Note that your container will have read only access to the `/data` folder. You can store large temporary files in the `/wdata` folder.

#### Custom docker options
In some cases it may be necessary to pass custom options to the `docker` or `nvidia-docker` commands. If you need such flags, you should list them in a file named `flags.txt` and place this file in the `/code` folder of your submission. The file must contain a single line only. If this file exists then its content will be added to the options list of the `docker run` command.

Example:

If `flags.txt` contains:
```
--ipc=host --shm-size 4G
```
then the docker command will look like:
```
docker run --ipc=host --shm-size 4G -v <local_data_path>:/data:ro -v <local_writable_area_path>:/wdata -it <id>
```

## Train and test scripts

Your container must contain a train and test (a.k.a. inference) script having the following specification. See the problem statement for further, problem specific requirements like the allowed time limits for these scripts.

### train.sh

`train.sh <data-folder>` should create any data files that your algorithm needs for running `test.sh` later. The supplied `<data-folder>` parameter points to a folder having training data in the same structure as is available for you during the coding phase. You may assume that the data folder path will be under `/data` within your container.

As its first step `train.sh` must delete the self-created models shipped with your submission.

Some algorithms may not need any training at all. It is a valid option to leave `train.sh` empty, but the file must exist nevertheless.

Training should be possible to do with working with only the contest's own training data and publicly available external data. This means that this script should do all the preprocessing and training steps that are necessary to reproduce your complete training work flow.

A sample call to your training script:
```
./train.sh /data/train/
```

In this case you can assume that the training data looks like this:
```
data/
    train/
        // all raw training data,
        // e.g. images and annotations
```

### test.sh

`test.sh <data-folder> <output_path>` should run your inference code using new, unlabeled data and should generate an output CSV file, as specified by the problem statement. You may assume that the data folder path will be under `/data`.

Inference should be possible to do without running training first, i.e. using only your prebuilt model files.

It should be possible to execute your inference script multiple times on the same input data or on different input data. You must make sure that these executions don't interfere, each execution leaves your system in a state in which further executions are possible.

A sample call to your testing script (single line):
```
./test.sh /data/test/ solution.csv
```
In this case you can assume that the testing data looks like this:
```
data/
    test/
        // all raw testing data,
        // e.g. unlabeled images
```
## Code requirements
Your training and inference scripts must output progress information. This may be as detailed as you wish but at the minimum it should contain the number of test cases processed so far.

Your testing code must process the test and validation data the same way, that is it must not contain any conditional logic based on whether it works on data that you have already downloaded or on unseen data.

Your `Dockerfile` must not contain `CMD` or `ENTRYPOINT` commands.

Your `Dockerfile` must contain a `WORKDIR` command that makes sure that when the container starts the `test.sh` and `train.sh` scripts will be found in the current directory.

To speed up the build process, it's recommended that your `Dockerfile` contains as many cacheable steps as possible. E.g. if there is a `COPY ./mymagic /work` command and the contents of the `/mymagic` folder changes from submission to sumbission (e.g. it contains the code you are working on) then this command should come only after everything else that stays static across submissions. 

## Verification workflow
1. `test.sh` is run on the provisional test set to verify that the results of your latest online submission can be reproduced. This test run uses your home built models.
2. `test.sh` is run on the final validation dataset, again using your home built models. Your final score is the one that your system achieves in this step.
3. `train.sh` is run on the full training dataset to verify that your training process is reproducible. After the training process finishes, further executions of the test script must use the models generated in this step.
4. `test.sh` is run on the final validation dataset (or on a subset of that), using the models generated in the previous step, to verify that the results achieved in step #2 above can be reproduced.

A note on reproducibility: we are aware that it is not always possible to reproduce the exact same results. E.g. if you do online training then the difference in the training environments may result in different number of iterations, meaning different models. Also you may have no control over random number generation in certain 3rd party libraries. In any case, the results must be statistically similar, and in case of differences you must have a convincing explanation why the same result can not be reproduced.

## A sample challenge
For demonstration only, this repository contains code for a hypothetical challenge in which your task is to predict weight of people based on their height. To illustrate the task, the `code/data` folder contains a simple training and testing file. These files generally need not be part of your submission, in this case this is added only so that you can test the sample code.

Assume that in this challenge `train.sh` is specified to take a single parameter: the location of a file containing training data. In a typical challange this would rather be a folder containg several files that store training data, but for simplicity we have a single training file now.

Similarly, `test.sh` takes two parameters: path to a testing file (again, in real challenges this is typically a folder) and an output file name.

Both these scripts forward their parameters to a solution written in Java, and they also pass an internal parameter: the location of a simple 'model' file. This demonstrates that the communication between the train and test scrips and the rest of your system is up to you, the testing environment is only interested in whether you comply to the input / output requirements of the train and test scripts.

During training the `sample.submission.Tester` class calculates linear regression parameters from the provided training data, which is written to `./model/dummy-model.txt` and this will be used during testing by the `sample.submission.Tester` class. Make sure that the model files required during testing are already packaged in your submission (or downloaded during building your container), so that testing is possible without running training first.

### Running the sample
Build the container from within the `/code` folder by
`docker build -t docker-template .`

Note that the build process makes sure that the Java files get compiled.

Launch the container with
`docker run -it docker-template`

Verify that testing works out of the box. Within the container, run
`./test.sh ./data/testing.txt ./data/solution.csv`

This should create a `solution.csv` file within the `/data` folder. This should be identical that is already present in the submission's `/solution` folder.

Verify that training works:
`./train.sh ./data/training.txt`

This should overwrite the `./model/dummy-model.txt` file, so subsequent testing will use the new model instead of the one shipped with the submission.

