import React, {Component} from 'react';
import {PropTypes} from 'prop-types';
import {Grid, Row, Text, View} from 'native-base';
import {Icon} from 'common/components';
import moment from 'moment';
import shotgun from 'native-base-theme/variables/shotgun';

export class OriginDestinationSummary extends Component{
  constructor(){
    super();
  }

  render(){
    const {contentType, delivery} = this.props;
    const {origin, destination, distance, duration} = delivery;

    const formatDuration = () => {
      const momentDuration = moment.duration(duration, 'seconds');

      return duration < 3600 ? `${momentDuration.minutes()}mins` : `${momentDuration.hours()}hrs`;
    };

    return <Grid>
      {contentType.origin ? <Row><Icon name="pin" paddedIcon originPin /><Text>{origin.line1}, {origin.postCode}</Text></Row> : null}
      <View style={styles.timeRow}>
        <Text time>
          {delivery.distance ? `${Math.round(distance / 1000)}kms` : null}{delivery.duration ? ` (${formatDuration()})` : null}
        </Text>
      </View>
      {contentType.destination ? <Row><Icon paddedIcon name="pin" /><Text>{destination.line1}, {destination.postCode}</Text></Row> : null}
    </Grid>;
  }
}

const styles = {
  timeRow: {
    borderLeftWidth: 2,
    borderColor: shotgun.silver,
    borderStyle: 'dashed',
    paddingLeft: 15,
    paddingTop: 5,
    paddingBottom: 5,
    marginLeft: 6
  }
};

OriginDestinationSummary.PropTypes = {
  origin: PropTypes.object,
  destination: PropTypes.object,
  delivery: PropTypes.object,
  contentType: PropTypes.object
};
