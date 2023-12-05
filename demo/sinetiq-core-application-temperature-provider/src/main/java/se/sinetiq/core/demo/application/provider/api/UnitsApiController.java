package se.sinetiq.core.demo.application.provider.api;

import se.sinetiq.core.demo.application.provider.model.Units;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.constraints.*;
import javax.validation.Valid;

import java.util.*;
import javax.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-06-20T08:44:02.600773+02:00[Europe/Stockholm]")
@Controller
@RequestMapping("${openapi.temperatureSensorAPIHTTPSJSON.base-path:}")
public class UnitsApiController implements UnitsApi {
    public ResponseEntity<Units> unitsGet() {
        return ResponseEntity.ok(new Units().units(Arrays.asList("C", "F", "K")));
    }
}