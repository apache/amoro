// Highlight the top nav as scrolling occurs
$('body').scrollspy({
    target: '.navbar-fixed-top'
})

// Closes the Responsive Menu on Menu Item Click
$('.navbar-collapse ul li a').click(function() {
    $('.navbar-toggle:visible').click();
});

$('div.modal').on('show.bs.modal', function() {
	var modal = this;
	var hash = modal.id;
	window.location.hash = hash;
	window.onhashchange = function() {
		if (!location.hash){
			$(modal).modal('hide');
		}
	}
});

$("#searchclear").click(function(){
    $("#search-input").val('');
    const results = document.querySelector('#search-results');
    while (results.firstChild) {
      results.removeChild(results.firstChild);
    }
});

// Coordinate control of codetabs
const languages = ["spark-sql", "spark-shell", "pyspark"]
const groups = {
    "spark-queries": [
        "spark-sql",
        "spark-shell",
        "pyspark"
    ],
    "spark-init": [
        "cli",
        "spark-defaults"
    ]
}
function hideCodeBlocks(group, type) {
    var codeblocks = document.querySelectorAll(`.${type}`);
    for (var i = 0; i < codeblocks.length; i++) {
    	  codeblocks[i].style.display = 'none';
    }
}

function showCodeBlocks(group, type) {
    var codeblocks = document.querySelectorAll(`.${type}`);
    for (var i = 0; i < codeblocks.length; i++) {
    	  codeblocks[i].style.display = 'block';
    }
}

function selectExampleLanguage(group, type) {
    groups[group].forEach(lang => hideCodeBlocks(group, lang));
	inputs = Array.from(document.getElementsByTagName('input')).filter(e => e.id == type);
    inputs.forEach(input => input.checked = true);
    console.log(inputs);
    showCodeBlocks(group, type);
}

// Set the default tab for each group
selectExampleLanguage("spark-queries", "spark-sql");
selectExampleLanguage("spark-init", "cli");