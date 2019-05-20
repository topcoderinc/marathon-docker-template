FROM openjdk:8-jdk-alpine3.9

# Create a /work directory within the container, copy everything from the
# build directory and switch there.
RUN mkdir /work
COPY . /work
WORKDIR /work

# Make sure your build script is executable. Generally, always check that 
# all your scripts are made executable, forgetting this is a common error. 
# Another frequent error is having Windows style line endings in .sh files. 
RUN chmod +x build.sh
RUN ./build.sh

# Again, test and train scripts should be executable within the container.
RUN chmod +x test.sh
RUN chmod +x train.sh

# Don't add any CMD or ENTRYPOINT!