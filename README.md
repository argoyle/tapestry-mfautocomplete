Description
===========
Tapestry-mfautocomplete is an autocomplete implementation for Tapestry 5 that can populate multiple fields.

It's implemented as a mixin to be mixed in to the textfield that should drive the autocompletion.

Usage
=====
Add a dependency to your POM:

    <dependency>
      <groupId>se.unbound</groupId>
      <artifactId>tapestry-mfautocomplete</artifactId>
      <version>1.1</version>
    </dependency>

Add the mixin to your textfield:

    <t:textfield t:id="description" size="30" value="description" t:mixins="mfautocomplete/MultifieldAutocomplete" 
    t:fields="['unitPrice','vatPercentage']" t:properties="['price','vat']"/>

The attribute t:fields names the additional fields to be populated and t:properties names the properties to fetch the values for 
the fields from. If t:properties is omitted, t:fields is used instead.

A method handling the providecompletions-event is needed in the page-class:

        public Item[] onProvideCompletionsFromDescription(final String description) {
          return items.matching(description);
        }

Item is a regular POJO with properties for description, price and vat as well as a toString-method returning the description.
The toString-method is called when the visible part of the completion is fetched.

