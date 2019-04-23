# marathon-docker-template
Template code for participating in Topcoder Marathon Matches


## Submission format
Our template supports both the "submit data" and "submit code" submission styles. Your submission should be a single ZIP file not larger than 500 MB, with the following content:

'''
/solution
   solution.csv
/code
   Dockerfile
   <your code>
'''   
   
, where /solution/solution.csv is the output your algorithm generates on the provisional test set. The format of this file is described above in the Output file section.
/code contains a dockerized version of your system that will be used to reproduce your results in a well defined, standardized way. This folder must contain a Dockerfile that will be used to build a docker container that will host your system during final testing. How you organize the rest of the contents of the /code folder is up to you, as long as it satisfies the requirements listed below in the Final testing section.

### Notes:
During provisional testing only your solution.csv file will be used for scoring, however the tester tool will verify that your submission file confirms to the required format. This means that at least the /code/Dockerfile must be present from day 1, even if it doesn't describe any meaningful system to be built. However, we recommend that you keep working on the dockerized version of your code as the challenge progresses, especially if you are at or close to a prize winning rank on the provisional leaderboard. 	
Make sure that your submission package is smaller than 500 MB. This means that if you use large files (external libraries, data files, pretained model files, etc) that won't fit into this limit, then your docker build process must download these from the net during building. There are several ways to achieve this, e.g. external libraries may be installed from a git repository, data files may be downloaded using wget or curl from Dropbox or Google Drive or any other public file hosting service. In any case always make sure that your build process is carefully tested end to end before you submit your package for final testing.
During final testing your last submission file will be used to build your docker container.
Make sure that the contents of the /solution and /code folders are in sync, i.e. your solution.csv file contains the exact output of the current version of your code.	
To speed up the final testing process the contest admins may decide not to build and run the dockerized version of each contestant's submission. It is guaranteed however that if there are N main prizes then at least the top 2*N ranked submissions (based on the provisional leader board at the end of the submission phase) will be final tested.
 	
## Final testing

To be able to successfully submit your system for final testing, some familiarity with Docker is required. If you have not used this technology before then you may first check this page and other learning material linked from there. To install docker follow these instructions. 
Contents of the /code folder
The /code folder of your submission must contain:
1. All your code (training and inference) that are needed to reproduce your results.
2. A dockerfile (named Dockerfile, without extension) that will be used to build your system.
3. All data files that are needed during training and inference, with the exception of	
the contests own training and testing data. You may assume that the contents of the /Training folder and the training annotations (as described in the Input files section) will be available on the machine where your docker container runs, compressed files already unpacked,
large data files that can be downloaded automatically either during building or running your docker script. 		
4. Your trained model file(s). Alternatively your build process may download your model files from the network. Either way, you must make it possible to run inference without having to execute training first.	

The tester tool will unpack your submission, and the
'''
docker build -t <id> .
'''
command will be used to build your docker image (the final . is significant), where <id> is your TopCoder handle. 
The build process must run out of the box, i.e. it should download and install all necessary 3rd party dependencies, either download from internet or copy from the unpacked submission all necessary external data files, your model files, etc.
Your container will be started by the
'''
docker run -v <local_data_path>:/data:ro -v <local_writable_area_path>:/wdata -it <id>
'''
command (single line), where the -v parameter mounts the contests data to the containers /data folder. This means that all the raw contest data will be available for your container within the /data folder. Note that your container will have read only access to the /data folder. You can store large temporary files in the /wdata folder.

To validate the template file supplied with this repo.  You can execute the following command:
'''
docker run -it <id>
'''

## Training and test scripts

Your container must contain a train and test (a.k.a. inference) script having the following specification: 
train.sh <data-folder> should create any data files that your algorithm needs for running test.sh later. The supplied <data-folder> parameters point to a folder having training data in the same structure as is available for you during the coding phase. The allowed time limit for the train.sh script is 3 days. You may assume that the data folder path will be under /data. 	
As its first step train.sh must delete the self-created models shipped with your submission. 	
Some algorithms may not need any training at all. It is a valid option to leave train.sh empty, but the file must exist nevertheless. 	
Training should be possible to do with working with only the contest's own training data and publicly available external data. This means that this script should do all the preprocessing and training steps that are necessary to reproduce your complete training work flow. 	
A sample call to your training script (single line):
'''
 	./train.sh /data/training/
'''

In this case you can assume that the training data looks like this:
'''
 	 data/
 	   training/
 	   TODO fill after structure fixed
'''

test.sh <data-folder> <output_path> should run your inference code using new, unlabeled data and should generate an output CSV file, as specified by the problem statement. The allowed time limit for the test.sh script is 24 hours. The testing data folder contain similar data in the same structure as is available for you during the coding phase. The final testing data will be similar in size and in content to the provisional testing data. You may assume that the data folder path will be under /data. 	
Inference should be possible to do without running training first, i.e. using only your prebuilt model files. 	
It should be possible to execute your inference script multiple times on the same input data or on different input data. You must make sure that these executions don't interfere, each execution leaves your system in a state in which further executions are possible.	
A sample call to your testing script (single line):
'''
 	./test.sh /data/test/ solution.csv
'''
In this case you can assume that the testing data looks like this:
'''
 	 data/
 	   test/
 	    TODO fill
'''
 

## Verification workflow
1. test.sh is run on the provisional test set to verify that the results of your latest online submission can be reproduced. This test run uses your home built models.
2. test.sh is run on the final validation dataset, again using your home built models. Your final score is the one that your system achieves in this step. 	
3. train.sh is run on the full training dataset to verify that your training process is reproducible. After the training process finishes, further executions of the test script must use the models generated in this step. 	
4. test.sh is run on the final validation dataset (or on a subset of that), using the models generated in the previous step, to verify that the results achieved in step #2 above can be reproduced. 
A note on reproducibility: we are aware that it is not always possible to reproduce the exact same results. E.g. if you do online training then the difference in the training environments may result in different number of iterations, meaning different models. Also you may have no control over random number generation in certain 3rd party libraries. In any case, the results must be statistically similar, and in case of differences you must have a convincing explanation why the same result can not be reproduced.

