import React, {Component} from 'react';
import {Grid, Row, Text} from 'native-base';
import {Icon} from 'common/components';
import moment from 'moment';
import shotgun from 'native-base-theme/variables/shotgun';
import {addressToText} from 'common/components/maps/MapUtils';
import * as ContentTypes from 'common/constants/ContentTypes';

export class OriginDestinationSummary extends Component{
  constructor(){
    super();
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  render(){
    const {resources} = this;
    const {delivery} = this.props;
    const {origin, destination = {}, distance, duration} = delivery;
    const {supportsOrigin, supportsDestination} = resources;

    const formatDuration = () => {
      const momentDuration = moment.duration(duration, 'seconds');
      return duration < 3600 ? `${momentDuration.minutes()}mins` : `${momentDuration.hours()}hrs`;
    };

    return <Grid>
      {supportsOrigin ? <Row>
        <Icon name="pin" paddedIcon originPin/>
        <Text style={styles.originText} numberOfLines={1}>{addressToText(origin)}</Text>
      </Row> : null}
      {supportsDestination ? <Row style={styles.timeRow}>
        <Icon name="dashed" style={styles.dashedIcon}/><Text time style={styles.timeText}>
          {delivery.distance ? `${Math.round(distance / 1000)}kms` : null}{delivery.duration ? ` (${formatDuration()})` : null}
        </Text>
      </Row> : null}
      {supportsDestination ? <Row>
        <Icon paddedIcon name="pin" />
        <Text style={styles.originText} numberOfLines={1}>{addressToText(destination)}</Text>
      </Row> : null}
    </Grid>;
  }
}

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('supportsOrigin', true).
  property('supportsDestination', true).
    skip(false).
    hire(false).
    rubbish(false);
/*eslint-enable */

const styles = {
  timeRow: {
    marginLeft: 6,
    alignItems: 'center'
  },
  originText: {
    alignSelf: 'flex-start'
  },
  timeText: {
    alignSelf: 'flex-start',
    paddingTop: 3,
    paddingLeft: 3
  },
  dashedIcon: {
    color: shotgun.silver,
    fontSize: 30,
    height: 22,
    marginRight: 15
  }
};