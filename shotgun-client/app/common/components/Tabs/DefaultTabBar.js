import React from 'react';
import PropTypes from 'prop-types';
import createReactClass from 'create-react-class';
const ReactNative = require('react-native');
import { connectStyle } from 'native-base-shoutem-theme';
import mapPropsToStyleNames from 'common/components/utils/mapPropsToStyleNames';
import variable from 'native-base-theme/variables/platform';
import { TabHeading, Text, TabContainer } from 'native-base';
import _ from 'lodash';

const {Animated} = ReactNative;
import { ViewPropTypes as RNViewPropTypes } from 'react-native';
const ViewPropTypes = RNViewPropTypes || View.propTypes;
const Button = require('./Button');

const DefaultTabBar = createReactClass({
  propTypes: {
    goToPage: PropTypes.func,
    activeTab: PropTypes.number,
    tabs: PropTypes.array,
    backgroundColor: PropTypes.string,
    activeTextColor: PropTypes.string,
    inactiveTextColor: PropTypes.string,
    tabStyle: ViewPropTypes.style,
    renderTab: PropTypes.func,
    underlineStyle: ViewPropTypes.style,
    tabContainerStyle: ViewPropTypes.style,
  },
  contextTypes: {
    theme: PropTypes.object,
  },

  getDefaultProps() {
    return {
      activeTextColor: variable.topTabBarActiveTextColor,
      inactiveTextColor: variable.topTabBarTextColor,
      backgroundColor: null,
    };
  },

  renderTabOption() {},

  renderTab(
    name,
    page,
    isTabActive,
    onPressHandler,
    tabStyle,
    activeTabStyle,
    textStyle,
    activeTextStyle,
    tabHeaderStyle
  ) {
    const headerContent = typeof name !== 'string' ? name.props.children : undefined;
    if (typeof name === 'string') {
      return (
        <Button style={{ flex: 1 }} key={name} onPress={() => onPressHandler(page)}>
          <TabHeading style={isTabActive ? activeTabStyle : tabStyle} active={isTabActive}>
            <Text  key={name} style={isTabActive ? activeTextStyle : textStyle}>{name}</Text>
          </TabHeading>
        </Button>
      );
    }
    return (
      <Button style={{ flex: 1 }} key={_.random(1.2, 5.2)} onPress={() => onPressHandler(page)}>
        <TabHeading style={tabHeaderStyle} active={isTabActive}>
          {headerContent}
        </TabHeading>
      </Button>
    );
  },

  render() {
    const variables = this.context.theme
      ? this.context.theme['@@shoutem.theme/themeStyle'].variables
      : variable;
    const containerWidth = this.props.containerWidth;
    const numberOfTabs = this.props.tabs.length;
    const tabUnderlineStyle = {
      position: 'absolute',
      width: containerWidth / numberOfTabs,
      height: 4,
      backgroundColor: variables.topTabBarActiveBorderColor,
      bottom: 0,
    };

    const left = this.props.scrollValue.interpolate({
      inputRange: [0, 1],
      outputRange: [0, containerWidth / numberOfTabs],
    });
    return (
      <TabContainer style={this.props.tabContainerStyle ? this.props.tabContainerStyle : {}}>
        {this.props.tabs.map((name, page) => {
          const isTabActive = this.props.activeTab === page;
          const renderTab = this.props.renderTab || this.renderTab;
          return renderTab(
            name,
            page,
            isTabActive,
            this.props.goToPage,
            this.props.tabStyle[page],
            this.props.activeTabStyle[page],
            this.props.textStyle[page],
            this.props.activeTextStyle[page],
            this.props.tabHeaderStyle[page]
          );
        })}
        <Animated.View style={[tabUnderlineStyle, { left }, this.props.underlineStyle]} />
      </TabContainer>
    );
  },
});

const StyledTab = connectStyle('NativeBase.DefaultTabBar', {}, mapPropsToStyleNames)(DefaultTabBar);
export { StyledTab as DefaultTabBar };
