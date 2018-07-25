
var gAddPositionMargin = function(beacons) {
	var addEqaulpos = function(target,addMargin) {
		target.pos.x += addMargin;
		target.pos.y += addMargin -1.5;
		return target;
	}
	var addEqaulpos2x2 = function(target,addMargin,vCount) {
		target.pos.x += addMargin;
		var marginY = 2;
		if(vCount % 2== 0){
			target.pos.y = (target.pos.y + (marginY*vCount));	
		}else{
			target.pos.y = (target.pos.y + (marginY*vCount));
		}
		
		return target;
	}
	var addMargin = function(pos, dx, dy) {
		pos.x += ( dx * PIN_MARGIN_X );
        pos.y += ( dy * PIN_MARGIN_Y );
	}

	// 同じposのbeaconの位置をずらす
	gBeaconPosition.getPosition().forEach(function(pos) {
        var index = 0;
		var targets = beacons.filter(function(b) {
		    if (b.pos.posId == pos.id && pos.id!= -1){
		        b.pos.size = pos.size
		    }
			return (b.pos.posId == pos.id && b.posSittingType != -1);
		});

		targets.forEach(function(target) {
            target.totalCount = targets.length
            target.posId = target.pos.posId
            if(targets.length >= VIEW_COUNT){
                target.overCheck = true
            }
        });
		if (targets.length > 1 && targets.length < VIEW_COUNT) {
			var viewType = targets[0].pos.viewType;
			if (viewType != null) {
				// alert(viewType);
				// 増加地m
				if (viewType == "3x3") {
                    var marginX = targets[0].pos.margin;
                    var arPoint = [
                        [0,0],[-(marginX),0],[marginX,0],
                        [0,marginX],[0,-(marginX)],[-(marginX),(marginX)],
                        [marginX,marginX],[-(marginX),-(marginX)],[marginX,-(marginX)]
                    ]
                    var vPointLenth = arPoint.length
                    var vStaticValue = 0;
                    targets.forEach(function(element,i) {
                        var vIndex = i % vPointLenth
                        if( i!=0 &&　vIndex == 0 )
                          vStaticValue += 2;

                        var vMarginX = arPoint[vIndex][0] + vStaticValue
                        var vMarginY = arPoint[vIndex][1] + vStaticValue
                        addMargin(targets[i].pos, vMarginX, vMarginY);
                    });
                } else if (viewType == "3x3_table") {
					// nearby
					var marginXstatic = -(targets[0].pos.margin + 1);
					var marginXCotei = -(targets[0].pos.margin - 0.5);
					var marginX = -(targets[0].pos.margin - 0.5);
					var marginYstatic = (targets[0].pos.margin + 3.5);
					var marginY = (targets[0].pos.margin - 0.5);
					var lengthSize = targets.length;
					var vCount = 0;

					for (var i = 0; i < targets.length; i++) {

						if (i % 3 == 0 && i != 0) {

							if(i%6==0){
								marginX = marginXCotei;
								marginY = vCount;
								vCount++;
							}else{
								marginX = marginXCotei;
								marginY = marginY - marginYstatic;
							}

						}
						addMargin(targets[i].pos, marginX, marginY);
						marginX = marginX - (marginXstatic);

					}

				}else if (viewType == "4x4") {
					// nearby
					var marginXstatic = -(targets[0].pos.margin);
					var marginXCotei = -(16);
					var marginX = -(16);
					var marginYstatic = (targets[0].pos.margin);
					var marginY = 0;
					var lengthSize = targets.length;
					var vCount = 0;

					for (var i = 0; i < targets.length; i++) {

						if (i % 4 == 0 && i != 0) {

							if(i%8==0){
								marginX = marginXCotei;
								marginY = vCount;
								vCount++;
							}else{
								marginX = marginXCotei;
								marginY = marginY + marginYstatic;
							}

						}
						addMargin(targets[i].pos, marginX, marginY);
						marginX = marginX - (marginXstatic);

					}

				}else if (viewType == "5x5") {
					// nearby
					var marginXstatic = -(targets[0].pos.margin+0.5);
					var marginXCotei = -(16);
					var marginX = -(16);
					var marginYstatic = (targets[0].pos.margin);
					var marginY = -(targets[0].pos.margin-1);
					var lengthSize = targets.length;
					var vCount = 0;

					for (var i = 0; i < targets.length; i++) {

						if (i % 5 == 0 && i != 0) {

							if(i%10==0){
								marginX = marginXCotei;
								marginY = vCount;
								vCount++;
							}else{
								marginX = marginXCotei;
								marginY = marginY + marginYstatic;
							}

						}
						addMargin(targets[i].pos, marginX, marginY);
						marginX = marginX - (marginXstatic);

					}

				}else if (viewType == "1x4") {
					// nearby
					var marginXCotei =  -3;
					var marginYCotei =  -4;
					var marginX = -(targets[0].pos.margin - 0.5);
					var marginYstatic = (targets[0].pos.margin + 3.5);
					var marginY = (targets[0].pos.margin - 0.5);
					var lengthSize = targets.length;
					var marginCount = 0;
					for (var i = 0; i < targets.length; i++) {

						if (i % 1 == 0 && i != 0) {
							if(i%4==0){
								marginY = marginYCotei;
								marginX = marginXCotei;
								marginXCotei = marginXCotei -2;
								marginCount++;
							}
							marginY = marginY + marginYstatic;
						}
						addMargin(targets[i].pos, marginX, marginY);
					}


				} else if (viewType == "1x1_down") {
					// nearby
					var marginXstatic = -(targets[0].pos.margin + 1);
					var marginXCotei = -(targets[0].pos.margin - 0.5);
					var marginX = -(targets[0].pos.margin - 0.5);
					var marginYstatic = (targets[0].pos.margin + 3.5);
					var marginY = (targets[0].pos.margin - 0.5);
					var lengthSize = targets.length;

					for (var i = 0; i < targets.length; i++) {

						if (i % 1 == 0 && i != 0) {
							marginX = marginXCotei;
							marginY = marginY + marginYstatic;
						}
						addMargin(targets[i].pos, marginX, marginY);
						marginX = marginX - (marginXstatic);

					}

				} else if (viewType == "1-2_down") {
				    var DOWN_COUNT = 2;
                    // nearby
                    var marginCotei = targets[0].pos.margin;
                    var marginX = 0;
                    var marginY = 0;

                    for (var i = 0; i < targets.length; i++) {
                        if ((i % DOWN_COUNT) == 0 && i != 0) {
                            marginX = marginCotei;
                            marginY = 0;
                        } else if (i % 1 == 0 && i != 0) {
                            marginY = marginY + targets[0].pos.margin;
                        }
                        addMargin(targets[i].pos, marginX, marginY);
                    }
                } else if (viewType == "1x1_yoko") {
					// nearby
					var marginX = -(targets[0].pos.margin);
					var marginY = (targets[0].pos.margin);
					var lengthSize = targets.length;

					for (var i = 0; i < targets.length; i++) {
						addMargin(targets[i].pos, marginX, marginY);
						marginX = marginX - 4.5;
					}

				} else if (viewType == "1-10_yoko") {
				    var DOWN_COUNT = 10;
                    // nearby
                    var marginCotei = targets[0].pos.margin;
                    var marginX = 0;
                    var marginY = 0;

                    for (var i = 0; i < targets.length; i++) {
                        if ((i % DOWN_COUNT) == 0 && i >= targets[0].pos.display_limit) {
                            marginX = 0;
                            marginY = 0;
                        } else if ((i % DOWN_COUNT) == 0 && i != 0) {
                            marginX = 0;
                            marginY = marginCotei * (i / DOWN_COUNT);
                        } else if (i % 1 == 0 && i != 0) {
                            marginX = marginX + targets[0].pos.margin;
                        }
                        addMargin(targets[i].pos, marginX, marginY);
                    }
                } else if (viewType == "1-4_yoko") {
                    var DOWN_COUNT = 4;
                    // nearby
                    var marginCotei = targets[0].pos.margin + 1.5;
                    var marginX = 0;
                    var marginY = 0;

                    for (var i = 0; i < targets.length; i++) {
                        if ((i % DOWN_COUNT) == 0 && i >= targets[0].pos.display_limit) {
                            marginX = 0;
                            marginY = 0;
                        } else if ((i % DOWN_COUNT) == 0 && i != 0) {
                            marginX = 0;
                            marginY = marginCotei * (i / DOWN_COUNT);
                        } else if (i % 1 == 0 && i != 0) {
                            marginX = marginX + targets[0].pos.margin;
                        }
                        addMargin(targets[i].pos, marginX, marginY);
                    }
                } else if (viewType == "1-5_yoko") {
                    var DOWN_COUNT = 5;
                    // nearby
                    var marginCotei = targets[0].pos.margin - 0.5;
                    var marginX = 0;
                    var marginY = 0;

                    for (var i = 0; i < targets.length; i++) {
                        if ((i % DOWN_COUNT) == 0 && i >= targets[0].pos.display_limit) {
                            marginX = 0;
                            marginY = 0;
                        } else if ((i % DOWN_COUNT) == 0 && i != 0) {
                            marginX = 0;
                            marginY = marginCotei * (i / DOWN_COUNT);
                        } else if (i % 1 == 0 && i != 0) {
                            marginX = marginX + targets[0].pos.margin;
                        }
                        addMargin(targets[i].pos, marginX, marginY);
                    }
                } else if (viewType == "1x1_right") {
					// nearby
					var marginX = -(targets[0].pos.margin);
					var marginY = (targets[0].pos.margin);
					var lengthSize = targets.length;

					for (var i = 0; i < targets.length; i++) {
						addMargin(targets[i].pos, marginX, marginY);
						marginX = marginX + 4.5;
					}

				}else if (viewType == "2x2") {
					// nearby
					var marginXstatic = -(targets[0].pos.margin + 1);
					var marginXCotei = -(targets[0].pos.margin - 0.5);
					var marginX = -(targets[0].pos.margin - 0.5);
					var marginYstatic = (targets[0].pos.margin + 1.2);
					var marginY = (targets[0].pos.margin - 2.5);
					var lengthSize = targets.length;
					var marginYcheck = false;
					var vAddon = 3;
					var vAddonCount = 0;
					var vArMargin = [0,-1,1,-2,2,3,-3,4,-4,5,-5];
					for (var i = 0; i < targets.length; i++) {
						if(vAddonCount != 0 && i  > (vAddon * vAddonCount)){
							targets[i] = addEqaulpos2x2(targets[i],vArMargin[vAddonCount],vAddonCount)
						}
						if (i % 4 == 3) {
							if (i != 0) {
								vAddonCount++;
								marginYcheck = true;
							}
						}
						if (i % 2 == 0 && i != 0) {
							marginX = marginXCotei;
							if(!marginYcheck){
								marginY = marginY - marginYstatic;
							}else{
								//marginY = -1;
								marginYcheck = false;
							}
						}
						addMargin(targets[i].pos, marginX, marginY);
						marginX = marginX - (marginXstatic);
					}

				}else if (viewType == "2x3") {
					// nearby
					var marginXstatic = -(targets[0].pos.margin + 1);
					var marginXCotei = -(targets[0].pos.margin - 0.5);
					var marginX = -(targets[0].pos.margin - 0.5);
					var marginYstatic = (targets[0].pos.margin + 1.2);
					var marginY = (targets[0].pos.margin - 2.5);
					var lengthSize = targets.length;
					var marginYcheck = false;
					var vAddon = 5;
					var vAddonCount = 0;
					var vArMargin = [0,-1,1,-2,2,3,-3,4,-4,5,-5];
					for (var i = 0; i < targets.length; i++) {
						if(vAddonCount != 0 && i  > (vAddon * vAddonCount)){
							targets[i] = addEqaulpos2x2(targets[i],vArMargin[vAddonCount],vAddonCount)
						}
						if (i % 6 == 5) {
							if (i != 0) {
								vAddonCount++;
								marginYcheck = true;
							}
						}
						if (i % 2 == 0 && i != 0) {
							marginX = marginXCotei;
							if(!marginYcheck){
								marginY = marginY - marginYstatic;
							}else{
								// marginY = -1;
								marginYcheck = false;
							}
						}
						addMargin(targets[i].pos, marginX, marginY);
						marginX = marginX - (marginXstatic);
					}

				}else if (viewType == "2x2_circle") {
					var marginXstatic = -(targets[0].pos.margin);
					var marginXCotei = -(targets[0].pos.margin);
					var marginX = targets[0].pos.margin;
					var marginYstatic = -(targets[0].pos.margin);
					var marginY = targets[0].pos.margin;
					var vCheck = false
					var vAddon = 12;
					var vArMargin = [3,-3,5,-5,7,-7,9,-9,11,-11,13.-13];
					var vAddonCount = 0;
					for (var i = 0; i < targets.length; i++) {
						if(vAddonCount != 0 && i  > (vAddon * vAddonCount)){
							targets[i] = addEqaulpos(targets[i],vArMargin[vAddonCount])
						}
						if (i % 12 == 0) {
							addMargin(targets[i].pos, 0, -(marginY));
						} else if (i % 13 == 1) {
							addMargin(targets[i].pos, 0, marginY);
						} else if (i % 14 == 2) {
							addMargin(targets[i].pos, -(marginX), 0);
						} else if (i % 15 == 3) {
							addMargin(targets[i].pos, marginX, 0);
						} else if (i % 16 == 4) {
							addMargin(targets[i].pos, -(marginX + 1),
									-(marginY + 1));
						} else if (i % 17 == 5) {
							addMargin(targets[i].pos, marginX + 1,
									-(marginY + 1));
						} else if (i % 18 == 6) {
							addMargin(targets[i].pos, -(marginX + 1),
									marginY + 1);
						} else if (i % 19 == 7) {
							addMargin(targets[i].pos, marginX + 1,
									marginY + 1);
						} else if (i % 20 == 8) {
							addMargin(targets[i].pos, 0, -(marginY
									+ marginY + 1));
						} else if (i % 21 == 9) {
							addMargin(targets[i].pos, 0, marginY + marginY
									+ 1);
						} else if (i % 22 == 10) {
							addMargin(targets[i].pos,
									-(marginX + marginX + 2), 0);
						} else if (i % 23 == 11) {
							if (i != 0) {
									vAddonCount++;

							}
							addMargin(targets[i].pos,
									(marginX + marginX + 2), 0);
						}
					}
				} else if (viewType == "all_circle") {
					var vArRoof = [1,1,1,1];
					var vTop = vArRoof[0]
					var vRight = vArRoof[1]
					var vBottom = vArRoof[2]
					var vLeft = vArRoof[3]

					var vTopCount = 0
					var vRightCount = 0
					var vBottomCount = 0
					var vLeftCount = 0

					var vTurnAround = vTop + vRight + vBottom +vLeft
					var vTurnAroundCount = 0;
					var vTurnTotalCount = 0;
					var vIncreValue = 2;
					var vMarginX = -1;
					var vMarginY = 1;

					var vStaticMarginX;
					var vStaticMarginY;
					var vRoofY = -2;
					var vRoofX = -2;
					var vMaxHaba = 4;


					for (var i = 0; i < targets.length; i++) {
						if(i == (targets.length-1)){
							addMargin(targets[i].pos,0, 0);
						}else{

							if(vTurnTotalCount >= vTurnAround){
								for(var j = 0; j < 4; j++){
									vArRoof[j] =vArRoof[j] +1;
								}
								vTop = vArRoof[0];
								vRight = vArRoof[1];
								vBottom = vArRoof[2];
								vLeft = vArRoof[3];
								vTurnAround = vTop + vRight + vBottom +vLeft;
								vTurnAroundCount = 0;
								vTurnTotalCount = 0;
								vRoofY = vRoofY-2;

								vTopCount = 0
								vRightCount = 0
								vBottomCount = 0
								vLeftCount = 0

						    }

							if(vTurnAroundCount < vTop){

								if(vTopCount == 0){
									vMarginX = 0;
									vMarginY =	-(vTop * vMaxHaba);
								}else{
									vMarginX = vMarginX + vMaxHaba;
									vMarginY = vMarginY + vMaxHaba;
								}
								addMargin(targets[i].pos,vMarginX, vMarginY);
								vTopCount++;
							}else {
								if (vTurnAroundCount< (vTop + vRight)){
									if(vRightCount == 0){
										vMarginX = (vRight * vMaxHaba);
										vMarginY =	0;
									}else{
										vMarginX = vMarginX - vMaxHaba;
										vMarginY =	vMarginY + vMaxHaba;

									}
									addMargin(targets[i].pos,vMarginX, vMarginY);
									vRightCount++;
								}else {
									if (vTurnAroundCount< (vTop + vRight +vBottom)){
										if(vBottomCount == 0){
											vMarginX = 0;
											vMarginY =	(vBottom * vMaxHaba);
										}else{
											vMarginX = vMarginX - vMaxHaba;
											vMarginY =	vMarginY - vMaxHaba;

										}
										addMargin(targets[i].pos,vMarginX, vMarginY);
										vBottomCount++;
									}else{
										if (vTurnAroundCount< (vTop + vRight +vBottom + vLeft)){
											if(vLeftCount == 0){
												vMarginX = -(vLeft * vMaxHaba);
												vMarginY =	0;

											}else{
												vMarginX = vMarginX + vMaxHaba;
												vMarginY = vMarginY - vMaxHaba;
											}
											addMargin(targets[i].pos,vMarginX, vMarginY);
											vLeftCount++;
										}
									}
								}
							}
							vTurnAroundCount++;
							vTurnTotalCount++;
					}
					}
				} else {
					var marginXstatic = -(targets[0].pos.margin + 1);
					var marginXCotei = -(targets[0].pos.margin - 0.5);
					var marginX = -(targets[0].pos.margin - 0.5);
					var marginYstatic = -(targets[0].pos.margin + 1.5);
					var marginY = -(targets[0].pos.margin - 2);
					var lengthSize = targets.length;

					for (var i = 0; i < targets.length; i++) {

						if (i % 2 == 0 && i != 0) {
							marginX = marginXCotei;
							marginY = marginY - marginYstatic;
						}
						addMargin(targets[i].pos, marginX, marginY);
						marginX = marginX - (marginXstatic);

					}

				}
			}
		}
		index++;
	});
}
