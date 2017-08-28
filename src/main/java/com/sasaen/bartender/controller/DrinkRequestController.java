package com.sasaen.bartender.controller;

import com.sasaen.bartender.response.DrinkResponse;
import com.sasaen.bartender.service.BartenderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
 * This Rest controller implements 2 endpoints to access the BartenderService
 * Created by santoss on 24/08/2017.
 */
@RequestMapping("/bartender")
@RestController
public class DrinkRequestController {

    @Autowired
    @Qualifier("bartenderService")
    private BartenderService service;

    public static Logger logger = LoggerFactory.getLogger(DrinkRequestController.class);

    @ApiOperation(value = "This endpoint requests a drink to the barman.",
            notes = "It will wait a number of milliseconds for the requested to be accepted.\n" +
                    "This timeout is configured in the property drink.request.timeout.milliseconds=2000\n",
            response = HttpStatus.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Drink request accepted"),
            @ApiResponse(code = 429, message = "Barman cannot take further requests"),
            @ApiResponse(code = 500, message = "Internal server error")}
    )
    @RequestMapping(value = "/request/{customerName}/{drinkType}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> requestDrink(
            @ApiParam(name = "customerName", value = "The customer name.", required = true)
            @PathVariable String customerName,
            @ApiParam(name = "drinkType", value = "The drink type, possible values: DRINK or BEER.", required = true)
            @PathVariable String drinkType) {
        DrinkResponse result = service.requestDrink(customerName, drinkType);
        ResponseEntity<?> resultResponseEntity = new ResponseEntity<>(result.isRequestAccepted() ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS);
        return resultResponseEntity;
    }


    /**
     * This endpoint returns the list of served drinks by the barman.
     *
     * Note: this list is kept in memory and not persisted between executions.
     *
     * @return a List of served drinks in Json format.
     * @throws Exception
     */
    @ApiOperation(value = "This endpoint returns the list of served drinks by the barman.",
            notes = "The list is kept in memory and not persisted between executions..\n",
            response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of served drinks"),
            @ApiResponse(code = 500, message = "Internal server error")}
    )
    @RequestMapping(value = "/served-drinks", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<DrinkResponse>> getServedDrinks()  {
        List<DrinkResponse> result = service.getServedDrinks();
        ResponseEntity<List<DrinkResponse>> resultResponseEntity = new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
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
