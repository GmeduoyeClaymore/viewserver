import React, {Component} from 'react';
import {PropTypes} from 'prop-types';
import {Grid, Row, Text} from 'native-base';
import {Icon} from 'common/components';
import moment from 'moment';
import shotgun from 'native-base-theme/variables/shotgun';
import {addressToText} from 'common/utils';
import * as ContentTypes from 'common/constants/ContentTypes';


/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('supportsOrigin', true).
  property('supportsDestination', true).
    skip(false).
    hire(false).
    rubbish(false);
/*eslint-enable */

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
        <Text style={{alignSelf: 'flex-start'}}>{addressToText(origin)}</Text>
      </Row> : null}
      {supportsDestination ? <Row style={styles.timeRow}>
        <Icon name="dashed" style={styles.dashedIcon}/><Text time style={styles.timeText}>
          {delivery.distance ? `${Math.round(distance / 1000)}kms` : null}{delivery.duration ? ` (${formatDuration()})` : null}
        </Text>
      </Row> : null}
      {supportsDestination ? <Row><Icon paddedIcon name="pin" /><Text>{destination.flatNumber} {destination.line1}, {destination.postCode}</Text></Row> : null}
    </Grid>;
  }
}

const styles = {
  timeRow: {
    marginLeft: 6,
    alignItems: 'center'
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

OriginDestinationSummary.PropTypes = {
  origin: PropTypes.object,
  destination: PropTypes.object,
  delivery: PropTypes.object,
  contentType: PropTypes.object
};
