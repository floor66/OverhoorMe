/* OverhoorMe JS toebehoren voor Android app
 * Online overhoring d.m.v. vlakjes over een afbeelding
 * v0.5 Alpha (fork van v0.5 Alpha webversie)
 * (C) 2014 F.P.J. den Hartog
 */

function overhoorMe(settings) {
	var main = this;
	main.obj = settings;
	main.STATUS_IDLE = 0;
	main.STATUS_EDIT = 1;
	main.counter = 0;
	main.vakjes = [];
	main.status = main.STATUS_EDIT;
	main.maxWidth = $(window).width() * 0.9;
	main.borderSize = 0;

	main.init = function() {
		$(".frame").append("<img id=\"tmpimage\" />");

		document.getElementById("tmpimage").onload = function() {
			var tmpSize = "100% auto";

			if($("#tmpimage").height() > main.maxWidth || $("#tmpimage").width() > main.maxWidth) {
				$("#tmpimage").css("width", main.maxWidth).css("height", "auto");
				tmpSize = main.maxWidth +"px auto";
			}
			
			$(".frame").css({
				"background": "url('"+ main.obj.tmpImgUri +"') center no-repeat",
				"background-size": tmpSize,
				"width": $("#tmpimage").width() + main.borderSize +"px",
				"height": $("#tmpimage").height() + main.borderSize +"px"
			});
			
			$("#tmpimage").remove();
		}
		
		document.getElementById("tmpimage").src = main.obj.tmpImgUri;
		
		$(".btn-group button").bind("click", function(e) {
			clearTimeout(main.timeout);
			$(this).parent().children("button").removeClass("btn-primary").removeClass("active").addClass("btn-default");
			$(this).removeClass(".btn-default").addClass("btn-primary").addClass("active");
			
			if($(this).attr("id") == "edit") {
				main.status = main.STATUS_EDIT;
				$("#opslaan, #buttons").fadeIn();
			} else {
				main.status = main.STATUS_IDLE;
				$("#opslaan, #buttons").fadeOut();
			}
			
			main.update();
			main.uitleg();
		});
		
		$(".btn-group").children("button").first().button("toggle");
		
		if(typeof main.obj.vakjes != "undefined") {
			if(main.obj.vakjes.length > 0) {
				main.vakjes = main.obj.vakjes;
				
				var highest = 0;
				for(var i = 0; i < main.vakjes.length; i++) {
					$(".frame").append("<div id=\""+ main.vakjes[i].id +"\" style=\"margin-top: "+ main.vakjes[i].margin_top +"; margin-left: "+ main.vakjes[i].margin_left +"; width: "+ main.vakjes[i].width +"; height: "+ main.vakjes[i].height +";\"></div>");
					$("#"+ main.vakjes[i].id).data("visible", true);
					main.bindVakje(main.vakjes[i].id.replace("vak_", ""));
					if(parseInt(main.vakjes[i].id.replace("vak_", "")) > highest) {
						highest = parseInt(main.vakjes[i].id.replace("vak_", ""));
					}
				}
				
				main.counter = highest + 1;
			}
		}

		main.update();
		main.uitleg();
	}
	
	main.bindVakje = function(id) {
		$("#vak_"+ id).unbind("touchstart").unbind("click");
		
		$("#vak_"+ id).bind("touchstart", function(e) {
			e.stopPropagation();
			
			var tmp = new Date();
			main.touchStarted = tmp.getTime();

			var self = this;
			self.waitTap = function(obj) {
				var tmp = new Date();
				var currTime = tmp.getTime();
				
				if((currTime - main.touchStarted) > 600 && main.status == main.STATUS_EDIT) {
					main.deleteVakje(obj);
				} else {
					main.timeout = setTimeout(function() {
						self.waitTap(obj);
					}, 100);
				}
			}
			
			self.waitTap($(this));
		});
		
		$("#vak_"+ id).bind("touchend", function(e) {
			e.stopPropagation();
			
			clearTimeout(main.timeout);
			var tmp = new Date();
			var currTime = tmp.getTime();
			
			if((currTime - main.touchStarted) < 600) {
				$(this).css("background", $(this).data("visible") == true ? "none" : "#999999");
				$(this).data("visible", $(this).data("visible") == true ? false : true);
			}
		});
	}
	
	main.uitleg = function() {
		if(main.status == main.STATUS_EDIT) {
			$(".uitleg").fadeOut(function() {
				$(".uitleg").html(
					"<h4>Uitleg:</h4>\Tik op de plek van de linkerbovenhoek van het vakje, en dan op de plek van de rechteronderhoek om het vakje te vormen.\n<br />"+
					"Tik op een bestaand vakje met de om het onderliggende <strong>(on)zichtbaar</strong> te maken.\n<br />"+ 
					"Tik en houdt een paar seconden vast om het vakje te <strong>verwijderen</strong>"
				);

				$(".uitleg").fadeIn();
			});
		} else if(main.status == main.STATUS_IDLE) {
			$(".uitleg").fadeOut(function() {
				$(".uitleg").html("<h4>Uitleg:</h4>\Tik op een vakje om het onderliggende <strong>(on)zichtbaar</strong> te maken.");

				$(".uitleg").fadeIn();
			});
		}
	}
	
	main.deleteVakje = function(obj) {
		for(var i = 0; i < main.vakjes.length; i++) {
			if(main.vakjes[i].id == obj.attr("id")) {
				main.vakjes.splice(i, 1);
			}
			
			obj.fadeOut(200, function() {
				obj.remove();
			});
		}
		
		main.update();
	}
	
	main.update = function() {
		$(".frame").unbind("click");
			
		if(main.status == main.STATUS_EDIT) {
			$(".frame").bind("click", function(e) {
				if($(e.target).attr("class") == "frame") {
					if(e.which == 1) {
						main.nieuwVakje(e);
					}
				}
			});
		}
		
		$("#save").attr("class", "btn btn-success").unbind("click").bind("click", function() {
			var tmp = main.obj.uri.split("/");
			tmp = tmp[tmp.length - 1].replace("image:", "");
			Android.saveVakjes(JSON.stringify({
				uri: main.obj.uri,
				imgId: tmp,
				vakjes: main.vakjes,
				frame: {
					width: $(".frame").width() - (2 * main.borderSize),
					height: $(".frame").height() - (2 * main.borderSize)
				}
			}));
		});
		
		$("#clear").unbind("click").bind("click", function() {
			$(".frame > div").unbind("touchstart", "touchend").remove();
			main.vakjes = {};
			main.counter = 0;
			
			main.update();
		});
	}

	main.nieuwVakje = function(e) {
		var self = this;
		$(".frame").append("<div id=\"vak_"+ main.counter +"\"></div>");
		
		self.vakje = $("#vak_"+ main.counter);
		self.vakje.css({
			"margin-left": e.pageX - $(".frame").offset().left +"px",
			"margin-top": e.pageY - $(".frame").offset().top +"px"
		}).data("visible", true);
		
				
		$(".frame").unbind("click").bind("click", function(e) {
			main.bindVakje(main.counter);
			
			var tapPos = {
				x: e.pageX - $(".frame").offset().left,
				y: e.pageY - $(".frame").offset().top,
			};
			
			var origin = {
				x: self.vakje.css("margin-left").replace("px", ""),
				y: self.vakje.css("margin-top").replace("px", ""),
			};
			
			if(tapPos.x > origin.x && tapPos.y < origin.y) {
				self.vakje.css({
					"margin-top": tapPos.y +"px"
				});
			} else if(tapPos.x < origin.x) {
				if(tapPos.y < origin.y) {
					self.vakje.css({
						"margin-left": tapPos.x +"px",
						"margin-top": tapPos.y +"px"
					});
				} else if(tapPos.y > origin.y) {
					self.vakje.css({
						"margin-left": tapPos.x +"px"
					});
				}
			}

			self.vakje.css({
				"width": Math.abs(tapPos.x - origin.x) +"px",
				"height": Math.abs(tapPos.y - origin.y) +"px"
			});

			main.vakjes.push({
				id: "vak_"+ main.counter,
				margin_top: self.vakje.css("margin-top"),
				margin_left: self.vakje.css("margin-left"),
				width: self.vakje.css("width"),
				height: self.vakje.css("height")
			});
			
			main.counter++;
			main.update();
		});
	}
	
	$(document).ready(function() {
		main.init();
	});
}
