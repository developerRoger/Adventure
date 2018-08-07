Vue.component('calendar',{
	template:`
		<div id="calendar-main" style="position:relative">
		<input v-if="validate" @click="openDateModal" :value="value" v-validate="validate" type="text" class="form-control" :disabled="disabled">
		<input v-else @click="openDateModal" :value="value" type="text" class="form-control" :disabled="disabled">
		<div id="calendar" v-show="show" style="z-index:999999;position:absolute;top:30px;right:0px">
			<div @click="hideDateModal" style="position:absolute;right:-12px;top:-12px;background:#FFF;border-radius:50%">
				<img src="/xframework/common/img/close.png" width="26"/>
			</div>
			<div class="month">
				<ul>
					<li class="arrow" @click="pickYear(-1)"><svg width="16" height="16" viewBox="0 0 16 16" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><g class="transform-group"><g transform="scale(0.015625, 0.015625)"><path d="M671.968 912c-12.288 0-24.576-4.672-33.952-14.048L286.048 545.984c-18.752-18.72-18.752-49.12 0-67.872l351.968-352c18.752-18.752 49.12-18.752 67.872 0 18.752 18.72 18.752 49.12 0 67.872l-318.016 318.048 318.016 318.016c18.752 18.752 18.752 49.12 0 67.872C696.544 907.328 684.256 912 671.968 912z" fill="#fff"></path></g></g></svg></li>
					<li class="arrow" @click="pickMonth(-1)"><svg width="16" height="16" viewBox="0 0 16 16" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><g class="transform-group"><g transform="scale(0.015625, 0.015625)"><path d="M671.968 912c-12.288 0-24.576-4.672-33.952-14.048L286.048 545.984c-18.752-18.72-18.752-49.12 0-67.872l351.968-352c18.752-18.752 49.12-18.752 67.872 0 18.752 18.72 18.752 49.12 0 67.872l-318.016 318.048 318.016 318.016c18.752 18.752 18.752 49.12 0 67.872C696.544 907.328 684.256 912 671.968 912z" fill="#fff"></path></g></g></svg></li>
					<li class="year-month">
					<span class="choose-year">{{ currentYear }}</span>
					<span class="choose-month">{{ currentMonth }}月</span>
					</li>
					<li class="arrow" @click="pickMonth(1)"><svg width="16" height="16" viewBox="0 0 16 16" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><g class="transform-group"><g transform="scale(0.015625, 0.015625)"><path d="M761.056 532.128c0.512-0.992 1.344-1.824 1.792-2.848 8.8-18.304 5.92-40.704-9.664-55.424L399.936 139.744c-19.264-18.208-49.632-17.344-67.872 1.888-18.208 19.264-17.376 49.632 1.888 67.872l316.96 299.84-315.712 304.288c-19.072 18.4-19.648 48.768-1.248 67.872 9.408 9.792 21.984 14.688 34.56 14.688 12 0 24-4.48 33.312-13.44l350.048-337.376c0.672-0.672 0.928-1.6 1.6-2.304 0.512-0.48 1.056-0.832 1.568-1.344C757.76 538.88 759.2 535.392 761.056 532.128z" fill="#fff"></path></g></g></svg></li>
					<li class="arrow" @click="pickYear(1)"><svg width="16" height="16" viewBox="0 0 16 16" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><g class="transform-group"><g transform="scale(0.015625, 0.015625)"><path d="M761.056 532.128c0.512-0.992 1.344-1.824 1.792-2.848 8.8-18.304 5.92-40.704-9.664-55.424L399.936 139.744c-19.264-18.208-49.632-17.344-67.872 1.888-18.208 19.264-17.376 49.632 1.888 67.872l316.96 299.84-315.712 304.288c-19.072 18.4-19.648 48.768-1.248 67.872 9.408 9.792 21.984 14.688 34.56 14.688 12 0 24-4.48 33.312-13.44l350.048-337.376c0.672-0.672 0.928-1.6 1.6-2.304 0.512-0.48 1.056-0.832 1.568-1.344C757.76 538.88 759.2 535.392 761.056 532.128z" fill="#fff"></path></g></g></svg></li>
				</ul>
			</div>
			<ul class="weekdays">
				<li>一</li>
				<li>二</li>
				<li>三</li>
				<li>四</li>
				<li>五</li>
				<li style="color:red">六</li>
				<li style="color:red">日</li>
			</ul>
			<ul class="days">
				<li @click="pick(day)" v-for="day in days" :class="{active:isActive(day)}">
					<span v-if="day.getMonth()+1 != currentMonth" class="other-month">{{ day.getDate() }}</span>
					<span v-else>
						<!--今天-->
						<span v-if="day.getFullYear() == new Date().getFullYear() && day.getMonth() == new Date().getMonth() && day.getDate() == new Date().getDate()" style="color:red">{{ day.getDate() }}</span>
						<span v-else>{{ day.getDate() }}</span>
					</span>
				</li>
			</ul>
		</div>
		</div>`,
	props: {
		callback:{},	//回调函数
		binding:{},		//绑定的数据对象{obj:object,field:'key'} obj:绑定的对象，field:值改变后，需要绑定的key
		value:'',		//显示在页面的值
		default:'',		//value没有值时，如果设置了默认值，会默认显示
		validate:{
			notNull:false
		},	//v-validate校验
		disabled:false, //禁用标识
    },
	data:function(){
		return {
			show:false,
			currentDay: 1,
			currentMonth: 1,
			currentYear: 1970,
			currentWeek: 1,
			days: [],
		}
	},
	created: function() {
		if(!this.value && this['default'])
			this.setValue(this['default']);
		this.initData(this.value);
	},
    methods: {
    	isActive(day){
    		var nowDay = this.formatDate(day.getFullYear(),(day.getMonth()+1),day.getDate());
    		return nowDay == this.value;
    	},
    	setValue:function(value){
    		this.binding.obj[this.binding.field] = value;
    		this.value = value;
    		this.show = false;
    	},
    	openDateModal:function(){
    		this.show = true;
    	},
    	hideDateModal:function(){
    		this.show = false;
    	},
    	initData: function(cur) {
			var date;
			if (cur) {
				date = new Date(cur);
			} else {
				date = new Date();
			}
			//从当月第一天开始渲染
			date = new Date(date.getFullYear(),date.getMonth(),1);
			this.currentDay = date.getDate();
			this.currentYear = date.getFullYear();
			this.currentMonth = date.getMonth() + 1;
			this.currentWeek = date.getDay(); // 1...6,0
			if (this.currentWeek == 0) {
				this.currentWeek = 7;
			}
			var str = this.formatDate(this.currentYear, this.currentMonth, this.currentDay);
			this.days.length = 0;
			// 今天是周日，放在第一行第7个位置，前面6个
			for (var i = this.currentWeek - 1; i >= 0; i--) {
				var d = new Date(str);
				d.setDate(d.getDate() - i);
				this.days.push(d);
			}
			for (var i = 1; i <= 35 - this.currentWeek; i++) {
				var d = new Date(str);
				d.setDate(d.getDate() + i);
				this.days.push(d);
			}
		},
		pick: function(date) {
			var dateStr = this.formatDate(date.getFullYear(), date.getMonth() + 1, date.getDate());
			this.setValue(dateStr);
			if(this.callback) 
				this.callback(dateStr);
		},
		pickMonth:function(num) {
			if(num == -1){
                if(this.currentMonth == 1){
                	this.currentYear -=1;
                	this.currentMonth = 12;
                } else {
                	this.currentMonth -= 1;
                }
            }else{
                if(this.currentMonth == 12){
                	this.currentYear +=1;
                	this.currentMonth = 1;
                }else {
                	this.currentMonth += 1;
                }
            }
			var d = new Date(this.currentYear,this.currentMonth-1,1)
			this.initData(this.formatDate(d.getFullYear(), d.getMonth()+1, 1));
		},
		pickYear: function(num) {
			var d = new Date(this.currentYear+num,this.currentMonth-1,1)
			this.initData(this.formatDate(d.getFullYear(), d.getMonth()+1, 1));
		},
		// 返回 类似 2016-01-02 格式的字符串
		formatDate: function(year, month, day) {
			var y = year;
			var m = month;
			if (m < 10) m = "0" + m;
			var d = day;
			if (d < 10) d = "0" + d;
			return y + "-" + m + "-" + d
		}
    }
});