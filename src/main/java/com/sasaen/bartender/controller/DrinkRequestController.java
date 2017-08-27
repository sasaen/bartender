package com.sasaen.bartender.controller;

import com.sasaen.bartender.result.Result;
import com.sasaen.bartender.service.BartenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by santoss on 24/08/2017.
 */
@RequestMapping("/bartender")
@RestController
public class DrinkRequestController {

    @Autowired
    @Qualifier("bartenderService")
    private BartenderService service;

    public static Logger logger = LoggerFactory.getLogger(DrinkRequestController.class);



    @RequestMapping(value = "/request/{customerId}/{drinkType}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> requestDrink(@PathVariable String customerId, @PathVariable String drinkType) throws Exception {
        Result result = service.requestDrink(customerId, drinkType);
        ResponseEntity<Result> resultResponseEntity = new ResponseEntity<>(result, result.isRequestAccepted() ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS);
        return resultResponseEntity;
    }



    @RequestMapping(value = "/served-drinks", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getServedDrinks() throws Exception {
        List<Result> result = service.getServedDrinks();
        ResponseEntity<List<Result>> resultResponseEntity = new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
        return resultResponseEntity;
    }


    @ExceptionHandler(value = Throwable.class)
    protected
    @ResponseBody
    ResponseEntity<?> handleThrowable(Throwable e, HttpServletRequest request) {
        logger.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
