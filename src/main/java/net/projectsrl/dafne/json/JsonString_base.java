
package net.projectsrl.dafne.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import net.project.errors.AppCrash;

public abstract class JsonString_base {

    private JsonGenerator _jsonGenerator;
    private OutputStream  _outputStream;

    public JsonString_base() {
        try {
            _outputStream = new ByteArrayOutputStream();
            _jsonGenerator = new JsonFactory().createGenerator(_outputStream);

        } catch (Throwable e) {
            new AppCrash(e);
        }
    }

    public JsonGenerator getJsonGenerator() {

        return _jsonGenerator;
    }

    public String createJsonString() throws IOException, AppCrash {

        _jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());

        _jsonGenerator.writeStartObject();

        addRootObject(_jsonGenerator);

        writeAdditionalObjects(_jsonGenerator);

        _jsonGenerator.writeEndObject(); // closing root object
        _jsonGenerator.flush();
        _jsonGenerator.close();

        String resultString = _outputStream.toString();
        return resultString;
    }

    protected abstract void writeAdditionalObjects(JsonGenerator jsonGenerator) throws AppCrash;

    protected abstract void addRootObject(JsonGenerator jsonGenerator) throws AppCrash;

}
