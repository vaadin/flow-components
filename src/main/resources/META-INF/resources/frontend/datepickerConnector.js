window.datepickerConnector = {
    initLazy: function(datepicker) {    
        // Check whether the connector was already initialized for the datepicker
        if (datepicker.$connector){
            return;
        }

        console.log("Initializing DatePicker Connector");

        datepicker.$connector = {};

        datepicker.$connector.setLocale = function(locale){
            try{
                // Check weather the locale is supported or not
                new Date().toLocaleDateString(locale);
            } catch (e){
                locale = "en-US";
                console.warn("The locale is not supported, use default locale setting(en-US).");
            }

            datepicker.i18n.formatDate = function(date){
                var rawDate = new Date(date.year,date.month,date.day);
                return rawDate.toLocaleDateString(locale);
            }

            datepicker.i18n.parseDate = function(dateString){
                const sample = "2009/12/31";
                const sample_parts = sample.split('/');
                var date = new Date();
                var sampleDate = new Date(sample);
                var sampleLocaleDate = sampleDate.toLocaleDateString(locale);

                if(sampleLocaleDate.toString() == sample) {
                    //Date format "YYYY/MM/DD"
                    var date = new Date(dateString);
                } else if (sampleLocaleDate.toString() == sample.split('/').reverse().join('/')){
                    //Date format "DD/MM/YYYY"
                    var date = new Date(dateString.split('/').reverse().join('/'));
                } else if (sampleLocaleDate.toString() == [sample_parts[1], sample_parts[2], sample_parts[0]].join('/')){
                    //Date format "MM/DD/YYYY"
                    const parts = dateString.split('/');
                    var date = new Date([parts[2],parts[0],parts[1]].join('/'));  
                } else {
                    console.warn("Selected locale is using unsupported date format, which might affect the parsing date.");
                    var date = new Date(dateString);
                }

                return {
                    day:date.getDate(),
                    month:date.getMonth(),
                    year:date.getFullYear()
                };
            }
        }
    }
}
