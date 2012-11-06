var loc;	var map;	var isFirst = 0;	var marker;	var bounds;	var zoomLevel = 14;
//var infowindow = new google.maps.InfoWindow({maxWidth:100});
var geocoder = new google.maps.Geocoder();
var currAddress;


function getLocInfo() {
	var xmlhttp;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			var posStr = xmlhttp.responseText.toString();
			var splits = posStr.split("/", 3);
			var latitude = splits[0];	var longitude = splits[1];	var dt = splits[2];
			loc = new google.maps.LatLng(latitude, longitude);

			if(isFirst==0){
				map = mapThisGoogle(loc, zoomLevel);
			}

			if((bounds = map.getBounds()) != null){
				if((loc.lat() < bounds.getNorthEast().lat() && loc.lat() > bounds.getSouthWest().lat())
						&& (loc.lng() < bounds.getNorthEast().lng() && loc.lng() > bounds.getSouthWest().lng())){
				}else{
					zoomLevel = map.getZoom();
					map = mapThisGoogle(loc, zoomLevel);
				}
			}
			revGeoCode(loc);
			updateLable(loc,dt);
			placeMarker(map, loc);
		};
	};
	xmlhttp.open("GET", "serve", true);
	xmlhttp.send();
	setTimeout("getLocInfo()", 3000);
}

function mapThisGoogle(loc, zoomLevel) {
	var myOptions = {
			center : loc,
			zoom : zoomLevel,
			mapTypeId : google.maps.MapTypeId.ROADMAP,
	};
	map = new google.maps.Map(document.getElementById("map_canvas"),myOptions);
	return map;
}

function placeMarker(map, loc){
	if(isFirst == 0){
		marker = new google.maps.Marker({
			position : loc,
			title : "Tushar's is here now..!",
		});
		isFirst = 1;
	}else{
		marker.setPosition(loc);
	}
	//infowindow.setContent(currAddress);
	//infowindow.open(map, marker);
	marker.setMap(map);
}

function updateLable(loc,dt){
	//var dt=new Date();
	document.getElementById("loc").innerHTML="Recent Latitude: "+loc.lat()+", Longitude: "+loc.lng()+" @ "+dt+"<br>Recent Address: "+currAddress;
}

function revGeoCode(loc){
	geocoder.geocode({'latLng': loc}, function(results, status) {
		if (status == google.maps.GeocoderStatus.OK) {
			if (results[0]) {
				currAddress = results[0].formatted_address;
			}
		} else {
			currAddress = "Geocoder failed due to: " + status;
		}
	});
}