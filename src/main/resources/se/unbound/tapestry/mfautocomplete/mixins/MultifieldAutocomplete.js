var MultifieldAutocomplete = Class.create({
  initialize : function(clientId, autocompleter, fields, simpleId) {
    this.clientId = clientId;
    this.autocompleter = autocompleter;
    this.fields = $A(fields.split(','));
    this.simpleId = simpleId;
  },

  updateFields : function(field, item) {
    var suffix = this.clientId.replace(this.simpleId, '');
    this.fields.each(function(field) {
      var fieldToSet=$(field + suffix);
      if (typeof(fieldToSet) !== 'undefined') {
        Form.Element.setValue(fieldToSet, item.readAttribute(field));
      } else {
        fieldToSet = $(field);
        if (typeof(fieldToSet) !== 'undefined') {
          Form.Element.setValue(fieldToSet, item.readAttribute(field));
        }
      }
    });
  }
});
