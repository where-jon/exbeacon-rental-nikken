###
	fillSignInForm = ($tr) ->
		$('#identifier, #email').val($tr.find('.email').text())
		$('#password').val($tr.find('.pwd ').text())


	$ ->
		$('#signin-helper tr').click (e) -> fillSignInForm $(this)
		alert "test"
###
