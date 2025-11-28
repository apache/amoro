'use strict';

(function () {

  const searchDataURL = '/search.json';

  const input = document.querySelector('#search-input');
  const results = document.querySelector('#search-results');

  if (!input) {
    return
  }

  input.addEventListener('focus', init);
  input.addEventListener('keyup', search);

  document.addEventListener('keypress', focusSearchFieldOnKeyPress);

  /**
   * @param {Event} event
   */
  function focusSearchFieldOnKeyPress(event) {
    if (event.target.value !== undefined) {
      return;
    }

    if (input === document.activeElement) {
      return;
    }

    const characterPressed = String.fromCharCode(event.charCode);
    if (!isHotkey(characterPressed)) {
      return;
    }

    input.focus();
    event.preventDefault();
  }

  /**
   * @param {String} character
   * @returns {Boolean} 
   */
  function isHotkey(character) {
    const dataHotkeys = input.getAttribute('data-hotkeys') || '';
    return dataHotkeys.indexOf(character) >= 0;
  }

  function init() {
    input.removeEventListener('focus', init); // init once
    input.required = true;
    fetch(searchDataURL)
      .then(pages => pages.json())
      .then(pages => {
        window.pages = pages;
      })
      .then(() => input.required = false)
      .then(search);
  }

  /**
   * This generates a search preview given the content and the query which is assumed
   * to be an exact-match query. extraLength defines how many characters before and after the
   * search match that you want to include. The search match is wrapped in a bold tag.
   * @param {String} content
   * @param {String} query
   * @param {Number} extraLength
   * @returns {String} 
   */
  function getSearchPreview(page, query, extraLength) {
      const resultLocation = page.content.toLowerCase().indexOf(query.toLowerCase());
      return `<a href="${page.uri}">...` +
      page.content.substring(resultLocation-extraLength, resultLocation) +
      "<b><mark>" +
      page.content.substring(resultLocation, resultLocation + query.length) +
      "</mark></b>" +
      page.content.substring(resultLocation + query.length, resultLocation + extraLength) +
      "..." +
      "</a>" +
      "<hr>"
  }
  function search() {
    while (results.firstChild) {
      results.removeChild(results.firstChild);
    }

    if (!input.value) {
      return;
    }

    const searchHits = window.pages.filter(page => page.content.toLowerCase().includes(input.value.toLowerCase()) )
    searchHits.forEach(function (page) {

      var contentPreview = getSearchPreview(page, input.value, 80);

      const li = element('<li><p></p><small></small></li>');
      const p = li.querySelector('p'), small = li.querySelector('small');
      p.textContent = page.title;
      small.innerHTML = contentPreview;

      results.appendChild(li);
    });
  }

  /**
   * @param {String} content
   * @returns {Node}
   */
  function element(content) {
    const div = document.createElement('div');
    div.innerHTML = content;
    return div.firstChild;
  }
})();