package request;

import file.FileContentType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Request {
    private final String method;

    private final String resource;

    private final String version;

    private final Map<String, String> requestHeader;

    private final String requestBody;

    private Request(String method, String resource, String version, List<String> requestHeaderList, String requestBody) {
        this.method = method;
        this.resource = resource;
        this.version = version;
        this.requestHeader = RequestParser.parseHeader(requestHeaderList);
        this.requestBody = requestBody;
    }

    public static Request from(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        String requestLine = bufferedReader.readLine();
        String method = RequestParser.parseMethod(requestLine);
        String resource = RequestParser.parseResource(requestLine);
        String version = RequestParser.parseVersion(requestLine);

        List<String> requestHeaderList = new ArrayList<>();

        String line;
        while(!(line = bufferedReader.readLine()).equals("")) {
            requestHeaderList.add(line);
        }

        StringBuilder stringBuilder = new StringBuilder();
        while(bufferedReader.ready()) {
            stringBuilder.append((char) bufferedReader.read());
        }

        return new Request(method, resource, version, requestHeaderList, stringBuilder.toString());
    }

    public String getMethod() {
        return method;
    }

    public String getResource() {
        return resource;
    }

    public String getVersion() {
        return version;
    }

    public String getResourceFileContentType() {
        int index = resource.lastIndexOf(".");
        String postfix = resource.substring(index+1);
        for(FileContentType fileContentType : FileContentType.values()) {
            if(postfix.equals(fileContentType.getPostfix())) {
                return fileContentType.getContentType();
            }
        }
        return FileContentType.NO_MATCH.getContentType();
    }

    public Map<String, String> getRequestHeader() {
        return requestHeader;
    }

    public String getCookie() {
        return requestHeader.get("Cookie") == null ? "" : requestHeader.get("Cookie").replace("sid=","");
    }

    public String getRequestBody() {
        return requestBody;
    }

    @Override
    public String toString() {
        return requestHeader.toString();
    }
}
