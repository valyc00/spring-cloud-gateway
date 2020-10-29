## A simple "curl" based SOAP Request 
## Which will invoke the HelloWorld Service:

request=`sed -e "s/\\$1/$1/" soap_request.xml`
curl -s -H "Content-Type: text/xml" -d "$request" http://localhost/EstrattoContoAppImpl/EstrattoContoService
echo
