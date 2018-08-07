Vue.component('xtabs', {
	template:
		'<div>' + 
			'<div class="x-tab-title">' + 
				'<ul v-for="(frame,index) in frames">' +
					'<li v-on:click="showTab(index)" :class="{active:frame.selected}">' +
						'{{frame.title}} <div v-on:click="removeTab(index)" class="x-tab-close"></div>' +
					'</li>' +
				'</ul>' +
			'</div>' +
			'<div v-for="frame in frames">' +
				'<iframe :height="height" width="100%" frameborder="0" :class="{show:frame.selected,hide:!frame.selected}" :src="frame.src" reload="XTabs.resize(this)"></iframe>' +
			'</div>' +
		'</div>',
	data:function() {
		return {
			frames:XTabs.data.FRAME_DATA,
			height:XTabs.data.FM_HEIGHT
		};
	},
	methods: {
		showTab:function(index){
			// 避免与关闭图标冲突(removeTab后会调用showTab)
			if (XTabs.data.FRAME_DATA[index]!=undefined) {
				XTabs.hideAll();
				XTabs.data.FRAME_DATA[index].selected=true;
			}
		},
		removeTab:function(index){
			XTabs.removeTab(index);
			XTabs.data.FRAME_DATA[XTabs.data.FRAME_DATA.length-1].selected=true;
		}
	}
});

var XTabs = {
	init: function(id,height,width) {
		XTabs.data.FM_HEIGHT = height - 36;
		var XTAB_VM = new Vue({
			el : id
		});
	},
	resize: function(fm_obj) {
		fm_obj = XTabs.data.FM_HEIGHT;
	},
	data: {
		FM_HEIGHT : 0,
		FRAME_DATA : []
	},
	hideAll: function() {
		for(var i=0; i<XTabs.data.FRAME_DATA.length; i++) {
			XTabs.data.FRAME_DATA[i].selected=false;
		}
	},
	addTab: function(title, src, selected) {
		for (i=0; i<XTabs.data.FRAME_DATA.length; i++) {
			if(title == XTabs.data.FRAME_DATA[i].title) {
				XTabs.hideAll();
				XTabs.data.FRAME_DATA[i].selected=true;
				return;
			}
		}
		XTabs.hideAll();
		XTabs.data.FRAME_DATA.push({
			title : title,
			src : src,
			selected : selected
		});
	},
	removeTab: function(index) {
		XTabs.data.FRAME_DATA.splice(index,1);
	}
}