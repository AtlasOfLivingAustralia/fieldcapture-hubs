/**
 * Attaches a bootstrap popover to the bound element.  The details for the popover should be supplied as the
 * value of this binding.
 * e.g.  <a href="#" data-bind="popover: {title:"Popover title", content:"Popover content"}>My link with popover</a>
 *
 * The content and title must be supplied, other popover options have defaults.
 *
 */
ko.bindingHandlers.popover = {

  init: function(element, valueAccessor) {
    ko.bindingHandlers.popover.initPopover(element, valueAccessor);
  },
  update: function(element, valueAccessor) {

    var $element = $(element);
    $element.popover('destroy');
    var options = ko.bindingHandlers.popover.initPopover(element, valueAccessor);
    if (options.autoShow) {
      if ($element.data('firstPopover') === false) {
        $element.popover('show');
        $('body').on('click', function(e) {

          if (e.target != element && $element.find(e.target).length == 0) {
            $element.popover('hide');
          }
        });
      }
      $element.data('firstPopover', false);
    }

  },

  defaultOptions: {
    placement: "right",
    animation: true,
    html: true,
    trigger: "hover"
  },

  initPopover: function(element, valueAccessor) {
    var options = ko.utils.unwrapObservable(valueAccessor());

    var combinedOptions = ko.utils.extend({}, ko.bindingHandlers.popover.defaultOptions);
    var content = ko.utils.unwrapObservable(options.content);
    ko.utils.extend(combinedOptions, options);
    combinedOptions.description = content;

    $(element).popover(combinedOptions);

    ko.utils.domNodeDisposal.addDisposeCallback(element, function() {
      $(element).popover("destroy");
    });
    return options;
  }
};

ko.bindingHandlers.independentlyValidated = {
  init: function(element, valueAccessor) {
    $(element).addClass('validationEngineContainer');
    $(element).find('thead').attr('data-validation-engine', 'validate'); // Horrible hack.
    $(element).validationEngine('attach', {scroll:false});
  }
};


ko.bindingHandlers.activityProgress = {
  update: function(element, valueAccessor) {
    var progressValue = ko.utils.unwrapObservable(valueAccessor());

    for (progress in ACTIVITY_PROGRESS_CLASSES) {
      ko.utils.toggleDomNodeCssClass(element, ACTIVITY_PROGRESS_CLASSES[progress], progress === progressValue);
    }
  }
}

ko.bindingHandlers.numeric = {
  init: function (element, valueAccessor) {
    $(element).on("keydown", function (event) {
      // Allow: backspace, delete, tab, escape, and enter
      if (event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
            // Allow: Ctrl+A
          (event.keyCode == 65 && event.ctrlKey === true) ||
            // Allow: . ,
          (event.keyCode == 190 || event.keyCode == 110) ||
            // Allow: home, end, left, right
          (event.keyCode >= 35 && event.keyCode <= 39)) {
        // let it happen, don't do anything
        return;
      }
      else {
        // Ensure that it is a number and stop the keypress
        if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105)) {
          event.preventDefault();
        }
      }
    });
  }
};

ko.bindingHandlers.slideVisible = {
  init: function(element, valueAccessor) {
    // Initially set the element to be instantly visible/hidden depending on the value
    var value = valueAccessor();
    $(element).toggle(ko.unwrap(value)); // Use "unwrapObservable" so we can handle values that may or may not be observable
  },
  update: function(element, valueAccessor) {
    // Whenever the value subsequently changes, slowly fade the element in or out
    var value = valueAccessor();
    ko.unwrap(value) ? $(element).slideDown() : $(element).slideUp();
  }
};

ko.bindingHandlers.booleanValue = {
  'after': ['options', 'foreach'],
  init: function(element, valueAccessor, allBindingsAccessor) {
    var observable = valueAccessor(),
        interceptor = ko.computed({
          read: function() {
            return (observable() !== undefined ? observable().toString() : undefined);
          },
          write: function(newValue) {
            observable(newValue === "true");
          }
        });

    ko.applyBindingsToNode(element, { value: interceptor });
  }
};

ko.bindingHandlers.onClickShowTab = {
  'init': function (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
    var originalFunction = valueAccessor();
    var newValueAccesssor = function() {
      return function () {
        var tabId = ko.utils.unwrapObservable(allBindingsAccessor().tabId);
        if (tabId) $(tabId).tab('show');
        originalFunction.apply(viewModel, arguments);
      }
    }
    ko.bindingHandlers.click.init(element, newValueAccesssor, allBindingsAccessor, viewModel, bindingContext);
  }
};