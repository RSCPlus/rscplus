var slides = $("slide");
var slide_index = Math.floor(Math.random() * slides.length);

function nextSlide()
{
	var index = Math.abs(slide_index % slides.length);

	slides.removeClass("show");
	slides.eq(index).addClass("show");

	slide_index++;
}

nextSlide();
setInterval("nextSlide();", 5000);
