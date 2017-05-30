cd WebRoot
jar -cvf searcher.war .
mv searcher.war ../
cd ..
sudo cp searcher.war /var/lib/tomcat8/webapps/
sudo cp install/searcher.xml /etc/tomcat8/Catalina/localhost/searcher.xml
sudo service tomcat8 restart
