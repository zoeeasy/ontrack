FROM openjdk:8-jdk

# Makes sure all tools for the build are installed
RUN apt-get update && apt-get install -y \
    git \
    subversion \
    zip \
    xvfb \
    iceweasel \
    graphviz \
    && rm -rf /var/lib/apt/lists/*

# Git configuration
RUN git config --global user.email "ci@nemerosa.net" \
    && git config --global user.name "CI"
