/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.CordaloController;
import ch.cordalo.corda.common.client.webserver.RpcConnection;
import ch.cordalo.corda.common.client.webserver.StateAndLinks;
import ch.cordalo.corda.common.client.webserver.StateBuilder;
import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.template.flows.CarFlow;
import ch.cordalo.template.states.CarState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;


@RestController
@RequestMapping("/api/v1/cordalo/template") // The paths for HTTP requests are relative to this base path.
public class ControllerCar extends CordaloController {

    private final static Logger logger = LoggerFactory.getLogger(ControllerCar.class);

    private final static String MAPPING_PATH = "/api/v1/cordalo/template";
    private final static String BASE_PATH = "/cars";


    public ControllerCar(RpcConnection rpcConnection) {
        super(rpcConnection);
    }


    private ResponseEntity<StateAndLinks<CarState>> getResponse(HttpServletRequest request, CarState car, HttpStatus status) throws URISyntaxException {
        return new StateBuilder<>(car, ResponseEntity.status(HttpStatus.OK))
                .stateMapping(MAPPING_PATH, BASE_PATH, request)
                .self()
                .build();
    }

    private ResponseEntity<List<StateAndLinks<CarState>>> getResponses(HttpServletRequest request, List<CarState> list, HttpStatus status) throws URISyntaxException {
        return new StateBuilder<>(list, ResponseEntity.status(HttpStatus.OK))
                .stateMapping(MAPPING_PATH, BASE_PATH, request)
                .self()
                .buildList();
    }

    /**
     * returns all unconsumed cars that exist in the node's vault.
     */
    @RequestMapping(
            value = BASE_PATH,
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<List<StateAndLinks<CarState>>> getCars(
            HttpServletRequest request) throws URISyntaxException {
        List<CarState> list = this.getProxy().vaultQuery(CarState.class).getStates()
                .stream().map(state -> state.getState().getData()).collect(toList());
        return this.getResponses(request, list, HttpStatus.OK);
    }

    /**
     * create a new car on import / cardossier
     *
     * @param request is the original http request to calculate links in response
     * @param model   of the car
     * @param make    of the car
     * @param type    of the car
     * @param stammNr of the car
     */
    @RequestMapping(
            value = BASE_PATH,
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<StateAndLinks<CarState>> createCar(
            HttpServletRequest request,
            @RequestParam(value = "make", required = true) String make,
            @RequestParam(value = "model", required = true) String model,
            @RequestParam(value = "type", required = true) String type,
            @RequestParam(value = "stammNr", required = true) String stammNr
    ) {
        if (stammNr == null || stammNr.isEmpty()) {
            return this.buildResponseFromException(HttpStatus.BAD_REQUEST, "stammNr cannot be empty.");
        }
        if (make == null || make.isEmpty()) {
            return this.buildResponseFromException(HttpStatus.BAD_REQUEST, "make cannot be empty.");
        }
        if (model == null || model.isEmpty()) {
            return this.buildResponseFromException(HttpStatus.BAD_REQUEST, "model cannot be empty.");
        }
        if (type == null || type.isEmpty()) {
            return this.buildResponseFromException(HttpStatus.BAD_REQUEST, "type cannot be empty.");
        }
        try {
            SignedTransaction signedTx = this.startFlow(
                    CarFlow.Create.class,
                    new UniqueIdentifier(), make, model, type, stammNr);

            StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
            CarState car = verifier.output().one(CarState.class).object();
            return this.getResponse(request, car, HttpStatus.CREATED);

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return this.buildResponseFromException(HttpStatus.EXPECTATION_FAILED, ex);
        }
    }

    /**
     * search a new car from cardossier / importer node
     *
     * @param request is the original http request to calculate links in response
     * @param stammNr of the car to search for
     */
    @RequestMapping(
            value = BASE_PATH + "/search",
            method = RequestMethod.GET,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<StateAndLinks<CarState>> searchCar(
            HttpServletRequest request,
            @RequestParam(value = "stammNr", required = true) String stammNr,
            @RequestParam(value = "from", required = true) String from
    ) {
        Party searchFromParty = this.partyFromString(from);
        if (searchFromParty == null) {
            return this.buildResponseFromException(HttpStatus.BAD_REQUEST, MessageFormat.format("from party {0} is not a valid peer", from));
        }
        try {
            CarState sharedCar = this.startFlow(
                    CarFlow.Search.class, stammNr, searchFromParty);
            return this.getResponse(request, sharedCar, HttpStatus.OK);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return this.buildResponseFromException(HttpStatus.EXPECTATION_FAILED, ex);
        }
    }

    /**
     * receives a unconsumed car with a given ID from the node's vault.
     *
     * @param id unique identifier as UUID for service
     */
    @RequestMapping(
            value = BASE_PATH + "/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<StateAndLinks<CarState>> getCar(
            HttpServletRequest request,
            @PathVariable("id") String id) throws URISyntaxException {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(uid),
                Vault.StateStatus.UNCONSUMED,
                null);
        List<CarState> cars = this.getProxy().vaultQueryByCriteria(queryCriteria, CarState.class)
                .getStates().stream().map(state -> state.getState().getData()).collect(toList());
        if (cars.isEmpty()) {
            return null;
        } else {
            CarState car = cars.get(cars.size() - 1);
            return this.getResponse(request, car, HttpStatus.OK);
        }
    }


}