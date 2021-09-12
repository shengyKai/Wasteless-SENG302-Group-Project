package cucumber.stepDefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SaleSearchStepDefinition {
    @And("the business {string} with the type {string} and location {string} exists")
    public void theBusinessWithTheTypeAndLocationExists(String name, String type, String location) {
    }

    @And("the business has the following products on sale:")
    public void theBusinessHasTheFollowingProductsOnSale() {
    }

    @When("no filters are passed")
    public void noFiltersArePassed() {
    }

    @Then("{int} sale items are returned")
    public void saleItemsAreReturned(int count) {
    }

    @When("orderBy is {string}")
    public void orderbyIs(String orderBy) {
    }

    @Then("products are in alphabetical order")
    public void productsAreInAlphabeticalOrder() {
    }

    @Then("products are in business order")
    public void productsAreInBusinessOrder() {
    }

    @Then("products are in location order")
    public void productsAreInLocationOrder() {
    }

    @Then("products are in quantity order")
    public void productsAreInQuantityOrder() {
    }

    @Then("products are in price order")
    public void productsAreInPriceOrder() {
    }

    @Then("products are in created date order")
    public void productsAreInCreatedDateOrder() {
    }

    @Then("products are in close date order")
    public void productsAreInCloseDateOrder() {
    }

    @When("businessType is {string}")
    public void businesstypeIs(String type) {
    }

    @When("search sale name is {string}")
    public void searchSaleNameIs(String name) {
    }

    @When("search sale price is between {int} and {int}")
    public void searchSalePriceIsBetweenAnd(int priceLower, int priceUpper) {
    }

    @When("search sale business is {string}")
    public void searchSaleBusinessIs(String busName) {
    }

    @When("search sale location is {string}")
    public void searchSaleLocationIs(String location) {
    }

    @When("search sale date is between {string} and {string}")
    public void searchSaleDateIsBetweenAnd(String closeLower, String closeUpper) {
    }
}
