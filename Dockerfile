# Pull base image
From tomcat:8-jre8

# Maintainer
MAINTAINER "Aymen Furter <aymen.furter@gmail.com">

# Copy to images tomcat path
ADD target/BRescue.war /usr/local/tomcat/webapps/ROOT.war
RUN rm -r /usr/local/tomcat/webapps/ROOT
RUN rm -r /usr/local/tomcat/webapps/manager
RUN rm -r /usr/local/tomcat/webapps/docs
RUN rm -r /usr/local/tomcat/webapps/host-manager
RUN rm -r /usr/local/tomcat/webapps/examples
