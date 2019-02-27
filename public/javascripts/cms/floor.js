function showFloorModal(a) {
    if (!a) {
        $('#inputFloorId').val('');
        $('#inputPreDisplayOrder').val('');
        $('#activeFlgDialog').val(false);
        $("#FLG_FILTER").val("1").prop("selected", true);
        $('#inputExbDeviceNoListComma').val('');
        $('#inputFloorName').val('');
        $('#inputDeviceNo').val('');
        $('.cloned').remove();
        $('#floorUpdateFooter').addClass('hidden');
        $('#floorRegisterFooter').removeClass('hidden');
    } else {
        $('.cloned').remove();
        $('#inputFloorId').val(a);
        $('#inputPreDisplayOrder').val($('#' + a).find('.displayOrder').text());
        $('#inputExbDeviceNoListComma').val('');
        var b = $('#' + a).find('.activeFlg').text();
        if (b == "表示") {
            $("#FLG_FILTER_DIALOG").val("1").prop("selected", true);
            $('#activeFlgDialog').val(true);
        } else {
            $("#FLG_FILTER_DIALOG").val("0").prop("selected", true);
            $('#activeFlgDialog').val(false);
        }
        $('#inputDisplayOrder').val($('#' + a).find('.displayOrder').text());
        $('#inputFloorName').val($('#' + a).find('.floorName').text());
        $('#inputDeviceNo').val('');
        $('#floorUpdateFooter').removeClass('hidden');
        $('#floorRegisterFooter').addClass('hidden');
    }
    $('#floorUpdateModal').modal();
}

function showFloorUpdateModal(a) {
    if (a) {
        showFloorModal();
    } else {
        if ($('.rowHoverSelectedColor').length > 0) {
            var b = $('.rowHoverSelectedColor').attr('data-floorId');
            showFloorModal(b);
        }
    }
}

function showFloorDeleteModal() {
    if ($('.rowHoverSelectedColor').length > 0) {
        var a = $('.rowHoverSelectedColor').attr('data-floorId');
        $('#deleteFloorId').val(a);
        $('#floorDeleteModal').modal();
    }
}

function addTagRow() {
    if ($('#inputDeviceId').val() != '') {
        if ($('#inputDeviceNo').val().match(/[0-9a-zA-Z]/)) {
            var c = false;
            $('.cloned').each(function (a, b) {
                if ($(b).find('span').text() == $('#inputDeviceNo').val()) {
                    c = true;
                    return false;
                }
            });
            if (c) {
                $('#inputDeviceNo').val('');
                return false;
            }
            var d = $('.template').clone();
            d.addClass('cloned');
            d.removeClass('template');
            d.find('span.inputDeviceNoSpan').text($.trim($('#inputDeviceNo').val()));
            var e = $('#inputExbDeviceNoListComma').val();
            if (e != "") {
                e = e + "-";
            }
            $('#inputExbDeviceNoListComma').val(e + $.trim($('#inputDeviceNo').val()));
            d.removeClass('hidden');
            $('.template').before(d);
            $('#inputDeviceNo').val('');
        }
    }
}

function removeTagRow(a) {
    var b = $(a).parent().parent();
    var c = $.trim(b.find('span.inputDeviceNoSpan').text());
    var d = $('#inputExbDeviceNoListComma').val();
    var e = new RegExp("(-" + c + "|" + c + "-|" + c + ")", '');
    $('#inputExbDeviceNoListComma').val(d.replace(e, ''));
    b.remove();
}

function btnEvent() {
    var d = $('#FLG_FILTER');
    if (d != null) {
        $('#FLG_FILTER').change(function () {
            var a = $('#FLG_FILTER option:selected').val();
            var b = document.getElementById("activeFlg");
            var c = false;
            if (a == 1) c = true;
            b.value = c;
        })
    }
    var e = $('#FLG_FILTER_DIALOG');
    if (e != null) {
        $('#FLG_FILTER_DIALOG').change(function () {
            var a = $('#FLG_FILTER_DIALOG option:selected').val();
            var b = document.getElementById("activeFlgDialog");
            var c = false;
            if (a == 1) c = true;
            b.value = c;
        })
    }
}

$(function () {
    btnEvent();
    gInitView.fixTable();
    gInitView.tableResize();
    gInitView.bindMouseAndTouch();
});