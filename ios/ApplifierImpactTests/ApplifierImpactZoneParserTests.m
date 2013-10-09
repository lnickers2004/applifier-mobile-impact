//
//  ApplifierImpactZoneParserTests.m
//  ApplifierImpact
//
//  Created by Ville Orkas on 10/9/13.
//  Copyright (c) 2013 Applifier. All rights reserved.
//

#import <SenTestingKit/SenTestingKit.h>

#import "ApplifierImpactZoneParser.h"
#import "ApplifierImpactIncentivizedZone.h"

@interface ApplifierImpactZoneParserTests : SenTestCase

@end

@implementation ApplifierImpactZoneParserTests

- (void)testZoneParserSingleZone {
  ApplifierImpactZone * zone = [ApplifierImpactZoneParser parseZone:@{@"id": @"testZone1", @"name": @"testZoneName1"}];
  STAssertTrue(zone != nil, @"Failed to parse valid zone");
}

- (void)testZoneParserIncentivizedZone {
  ApplifierImpactZone * zone = [ApplifierImpactZoneParser parseZone:@{@"id": @"testZone1", @"name": @"testZoneName1", @"incentivised": @"true"}];
  STAssertTrue([zone isKindOfClass:[ApplifierImpactIncentivizedZone class]], @"Failed to return an instance of an incentivised zone");
}

- (void)testZoneParserMultipleZones {
  NSDictionary * zones = [ApplifierImpactZoneParser parseZones:@[@{@"id": @"testZone1", @"name": @"testZoneName1"}, @{@"id": @"testZone2", @"name": @"testZoneName2"}]];
  STAssertTrue([zones count] == 2, @"Failed to parse multiple zones");
}

@end