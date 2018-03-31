import React from 'react';
import {Text, Header, Left, Body, Container, Button, Title, Content, Grid, Col} from 'native-base';
import {Icon} from 'common/components';

export default TermsAndConditions = ({history}) => {
  return <Container>
    <Header withButton>
      <Left>
        <Button onPress={() => history.goBack()}>
          <Icon name='back-arrow'/>
        </Button>
      </Left>
      <Body><Title>Terms of Service</Title></Body>
    </Header>
    <Content padded>
      <Grid>
        <Col>
          <Text>
            Last Updated 28th March 2018
          </Text>
          <Text style={styles.heading}>
            THIS FOLLOWING USER AGREEMENT DESCRIBES THE TERMS AND CONDITIONS ON
            WHICH SHOTGUN LTD. OFFERS YOU ACCESS TO THE SHOTGUN PLATFORM.
          </Text>
          <Text>
            These terms of service constitute a legally binding agreement (the
            “Agreement”) between you (“you,” or “your”) and Shotgun Ltd. (“Shotgun”
            “we,” “us” or “our”), a registered UK Company, governing your use of the
            Shotgun application, website, and technology platform (collectively, the
            “Shotgun Platform”).
          </Text>
          <Text>
            The Shotgun Platform provides a marketplace where persons (“Customers”) who
            seek services including but not limited to; delivery of goods, removal and
            disposal of commercial or household waste and hiring of trades people
            (“Services”) can be matched with persons (“Partners”) providing the
            aforementioned services. Partners and Customers are collectively referred
            to herein as “Users” and each User shall create a User account that enables
            such User to access the Shotgun Platform.
          </Text>
          <Text style={styles.heading}>
            SHOTGUN DOES NOT PROVIDE DELIVERY, WASTE REMOVAL OR PERSONELL SERVICES,
            AND SHOTGUN IS NOT A DELIVERY CARRIER. IT IS UP TO THE PARTNER TO
            DECIDE WHETHER OR NOT TO OFFER A SERVIC TO A CUSTOMER CONTACTED THROUGH
            THE SHOTGUN PLATFORM, AND IT IS UP TO THE CUSTOMER TO DECIDE WHETHER OR
            NOT TO ACCEPT A SERVICE FROM ANY PARTNER CONTACTED THROUGH THE SHOTGUN
            PLATFORM. ANY DECISION BY A USER TO OFFER OR ACCEPT SERVICES ONCE SUCH
            USER IS MATCHED THROUGH THE SHOTGUN PLATFORM IS A DECISION MADE IN SUCH
            USER’S SOLE DISCRETION. EACH SERVICE PROVIDED BY A PARTNER TO A
            CUSTOMER SHALL CONSTITUTE A SEPARATE AGREEMENT BETWEEN SUCH PERSONS.
          </Text>
          <Text>
            By creating your User account and using the Shotgun Platform, you expressly
            acknowledge that you understand this Agreement and accept all of its terms.
          </Text>
          <Text style={styles.heading}>
            IF YOU DO NOT AGREE TO BE BOUND BY THE TERMS AND CONDITIONS OF THIS
            AGREEMENT, YOU MAY NOT USE OR ACCESS THE SHOTGUN PLATFORM OR THE
            SERVICES.
          </Text>
          <Text style={styles.heading}>Modification to the Agreement</Text>
          <Text>
            We reserve the right to modify the terms and conditions of this Agreement
            at any time, effective upon posting the amended terms on this site. If we
            make changes, we will notify you by, at a minimum, revising the “Last
            Updated” date at the top of this Agreement. You are responsible for
            regularly reviewing this Agreement. Continued use of the Shotgun Platform
            or Services after any such changes shall constitute your consent to such
            changes. If you do not agree to any such changes you may not use or access
            the Shotgun Platform or the Services.
          </Text>
          <Text style={styles.heading}>Eligibility</Text>
          <Text>
            The Shotgun Platform is available only to, and may only be used by
            individuals who can form legally binding contracts under applicable law.
            Without limiting the foregoing, the Shotgun Platform is not available to
            children (persons under the age of 18) or Users who have had their User
            account temporarily or permanently deactivated. By becoming a User, you
            represent and warrant that you are at least 18 years old and that you have
            the right, authority and capacity to enter into and abide by the terms and
            conditions of this Agreement.
          </Text>
          <Text style={styles.heading}>Payments</Text>

          <Text>

            As a Customer, you agree that any amounts charged after a Service has been
            completed (a “Charge”) are mandatory and due and payable immediately upon
            completion of the ride. Shotgun has the authority and reserves the right to
            determine and modify pricing of new jobs created within the platform.
          </Text>
          <Text>
            • Fixed Fees. A fixed fee is a Service which is provided at a set monetary
            value. The Customer will create a request for a Service with a fixed fee.
            This fee may be negotiated between the Customer and the Partner and amended
            by the Customer. Once a Service has been started the Fixed fee cannot be
            amended
          </Text>
          <Text>
            • Duration Fee: A duration fee is a Service which is provided for a set
            number of days with each day being charged at a set monetary value
          </Text>
          <Text>
            • Job Duration Fee: A duration fee is a Service which is provided for a set
            number of hours with each hours being charged at a set monetary value, plus
            an additional fee for any additional labour required to complete the job
          </Text>
          <Text>
            • Journey Distance Fee: A journey distance fee is a Service which is
            charged at a set monetary value per kilometre between the start and end of
            the Service (ie for a Delivery) , plus an additional fee for any additional
            labour required to complete the job
          </Text>
          <Text>
            • Facilitation of Payments. All Charges are facilitated through a
            third-party payment processing service (Stripe). Shotgun may replace its
            third-party payment processing services without notice to you. Charges
            shall only be made through the Shotgun Platform.
          </Text>
          <Text>
            • No Refunds. Charges are assessed immediately following completion of the
            trip to your authorized payment method. All Charges are non-refundable.
            This no-refund policy shall apply at all times regardless of your decision
            to terminate usage of the Shotgun Platform, any disruption to the Shotgun
            Platform or Services, or any other reason whatsoever.
          </Text>
          <Text>
            • Promotions. Shotgun, at its sole discretion, may make available
            promotions with different features to any of our Customer or prospective
            Customers. These promotions, unless made to you, shall have no bearing
            whatsoever on your Agreement or relationship with Shotgun.
          </Text>
          <Text>
            • Credit Card Authorization. Upon addition of a new payment method or each
            trip request, Shotgun may seek authorization of your selected payment
            method to verify your payment method, ensure the ride cost will be covered,
            and protect against unauthorized behaviour. The authorization is not a
            charge, however, it may reduce your available credit by the authorization
            amount until your bank’s next processing cycle. Should the amount of our
            authorization exceed the total funds on deposit in your account, you may be
            subject to overdraft of NSF charges by the bank issuing your debit or check
            card. We cannot be held responsible for these charges and are unable to
            assist you in recovering them from your issuing bank.
          </Text>
          <Text style={styles.heading}>Partner Service Fees</Text>

          <Text>

            As a Partner, you will receive applicable Service fees (net of Shotgun’s
            Administrative Fee, as discussed below). Shotgun will process all payments
            due to you through its third party payments processor. You acknowledge and
            agree that such amounts shall not include any interest and will be net of
            any amounts that we are required to withhold by law. You expressly
            authorize Shotgun to set the prices on your behalf for all Charges that
            apply to the provision of Services. Shotgun reserves the right to withhold
            all or a portion of Service fees if it believes that you have attempted to
            defraud or abuse Shotgun or Shotgun’s payment systems.

            In exchange for permitting you to offer your Services through the Shotgun
            Platform and marketplace as a Partner, you agree to pay Shotgun (and permit
            Shotgun to retain) a fee based on each transaction in which you provide
            Services (the “Administrative Fee”) The amount of the applicable
            Administrative Fee will be communicated to you when you accept a Service.
            Shotgun reserves the right to change the Administrative Fee at any time in
            Shotgun’s discretion. Continued use of the Shotgun Platform after any such
            change in the Administrative Fee calculation shall constitute your consent
            to such change.

            Shotgun, at its sole discretion, may make available promotions with
            different features to any Partners or prospective Partners. These
            promotions, unless made to you, shall have no bearing whatsoever on your
            Agreement or relationship with Shotgun.
          </Text>
          <Text style={styles.heading}>Shotgun Communications</Text>

          <Text>

            By becoming a User, you expressly consent and agree to accept and receive
            communications from us, including via e-mail, text message, calls, and push
            notifications to the cellular telephone number you provided to us.
          </Text>
          <Text style={styles.heading}>
            Your Information
          </Text>
          <Text>

            Your Information is any information you provide, publish or post to or
            through the Shotgun Platform (including any profile information you
            provide) or send to other Users (including via in-application feedback, any
            email feature, or through any Shotgun-related Facebook, Twitter or other
            social media posting) (your “Information”). You consent to us using your
            Information to create a User account that will allow you to use the Shotgun
            Platform and participate in the Services.
          </Text>
          <Text style={styles.heading}>Privacy</Text>

          <Text>

            You are solely responsible for your Information and your interactions with
            other members of the public, and we act only as a passive conduit for your
            online posting of your Information. You agree to provide and maintain
            accurate, current and complete information and that we and other members of
            the public may rely on your Information as accurate, current and complete.

            You warrant and represent to us that you are the sole author of your
            Information. To enable the Shotgun Platform to use your Information, you
            grant to us a non-exclusive, worldwide, perpetual, irrevocable,
            royalty-free, sub-licensable (through multiple tiers) right and license to
            exercise the copyright, publicity, and database rights you have in your
            Information, and to use, copy, perform, display and distribute such
            Information to prepare derivative works, or incorporate into other works,
            such Information, in any media now known or not currently known. Shotgun
            does not assert any ownership over your Information; rather, as between us
            and you, subject to the rights granted to us in this Agreement, you retain
            full ownership of all of your Information and any intellectual property
            rights or other proprietary rights associated with your Information.

            You are the sole authorized user of your account. You are responsible for
            maintaining the confidentiality of any password provided by you or Shotgun
            for accessing the Shotgun Platform. You are solely and fully responsible
            for all activities that occur under your User account, and Shotgun
            expressly disclaims any liability arising from the unauthorized use of your
            User account. Should you suspect that any unauthorized party may be using
            your User account or you suspect any other breach of security, you agree to
            notify us immediately.
          </Text>
          <Text style={styles.heading}>Restricted Activities</Text>
          <Text>


            With respect to your use of the Shotgun Platform and your participation in
            the Services, you agree that you will not:

            a impersonate any person or entity;

            b stalk, threaten, or otherwise harass any person, or carry any weapons;

            c violate any law, statute, ordinance or regulation;

            d interfere with or disrupt the Services or the Shotgun Platform or the
            servers or networks connected to the Shotgun Platform;

            e post Information or interact on the Shotgun Platform or Services in a
            manner which is false, inaccurate, misleading (directly or by omission or
            failure to update information), defamatory, libellous, abusive, obscene,
            profane, offensive, sexually oriented, threatening, harassing, or illegal;

            f use the Shotgun Platform in any way that infringes any third party’s
            rights, including but not limited to: intellectual property rights,
            copyright, patent, trademark, trade secret or other proprietary rights or
            rights of publicity or privacy;

            g post, email or otherwise transmit any malicious code, files or programs
            designed to interrupt, damage, destroy or limit the functionality of any
            computer software or hardware or telecommunications equipment or
            surreptitiously intercept or expropriate any system, data or personal
            information;

            h forge headers or otherwise manipulate identifiers in order to disguise
            the origin of any information transmitted through the Shotgun Platform;

            i “frame” or “mirror” any part of the Shotgun Platform, without our prior
            written authorization or use meta tags or code or other devices containing
            any reference to us in order to direct any person to any other web site for
            any purpose; or

            j modify, adapt, translate, reverse engineer, decipher, decompile or
            otherwise disassemble any portion of the Shotgun Platform or any software
            used on or for the Shotgun Platform;

            k rent, lease, lend, sell, redistribute, license or sublicense the Shotgun
            Platform or access to any portion of the Shotgun Platform;

            l use any robot, spider, site search/retrieval application, or other manual
            or automatic device or process to retrieve, index, scrape, “data mine”, or
            in any way reproduce or circumvent the navigational structure or
            presentation of the Shotgun Platform or its contents;

            m create liability for us or cause us to become subject to regulation as a
            transportation carrier or provider of taxi service;

            n link directly or indirectly to any other web sites;

            o transfer or sell your User account, password and/or identification to any
            other party; or

            p cause any third party to engage in the restricted activities above.

            We reserve the right, but we have no obligation, to suspend or deactivate
            your User account if you do not comply with these prohibitions.
          </Text>
          <Text style={styles.heading}>Partner Representations and Warranties</Text>
          <Text>
            • You will be solely responsible for any and all liability that results
            from or is alleged as a result of your provision of Services, including,
            but not limited to personal injuries, death and property damages (however,
            this provision shall not limit the scope of Shotgun’s insurance policies
            referenced on.
          </Text>
          <Text>
            • You will comply with all applicable laws, rules and regulations while
            providing Services, and you will be solely responsible for any violations
            of such provisions.
          </Text>
          <Text>
            • You will pay all applicable income, corporation taxes and National
            Insurance based on your provision of Services and any payments received by
            you.
          </Text>
          <Text>
            • You will not make any misrepresentation regarding Shotgun, the Shotgun
            Platform, the Services or your status as a Partner or engage in any other
            activity in a manner that is inconsistent with your obligations under this
            Agreement.
          </Text>
          <Text>
            • You will not attempt to defraud Shotgun or Customers in connection with
            your provision of Services. If we suspect that you have engaged in
            fraudulent activity we may withhold applicable Ride Fees or other payments
            for the ride(s) in question.
          </Text>
          <Text>
            • You will not discriminate or harass anyone on the basis of race, national
            origin, religion, gender, gender identity, physical or mental disability,
            medical condition, marital status, age or sexual orientation.
          </Text>
          <Text>
            • You agree that we may obtain information about you, including your
            criminal and driving records, and you agree to provide any further
            necessary authorizations to facilitate our access to such records during
            the term of the Agreement.
          </Text>
          <Text style={styles.heading}>
            Driver Specific Representations and Warranties
          </Text>
          <Text>
            By providing Services as a delivery driver or waste removal driver
            (“Driver”) on the Shotgun Platform, you represent, warrant, and agree that:
          </Text>
          <Text>
            • You possess a valid driver’s license and are authorized and medically fit
            to operate a motor vehicle and have all appropriate licenses, approvals and
            authority to provide transportation to Customers in all jurisdictions in
            which you provide Services.
          </Text>
          <Text>
            • You own, or have the legal right to operate, the vehicle you use when
            providing Services, and such vehicle is in good operating condition and
            meets the industry safety standards and all applicable statutory and state
            department of motor vehicle requirements for a vehicle of its kind.
          </Text>
          <Text>
            • You will only provide Services using the vehicle that has been reported
            to Shotgun, and you will not transport anything which is heavier than the
            maximum load for your vehicle.
          </Text>
          <Text>
            • You have a valid policy of vehicle insurance and liability insurance (in
            coverage amounts consistent with all applicable legal requirements) that
            names or schedules you for the operation of the vehicle you use to provide
            Services.
          </Text>
          <Text>
            • In the event of a motor vehicle accident you will be solely responsible
            for compliance with any applicable statutory or department of motor
            vehicles requirements, for reporting the accident to Shotgun and your
            insurer in a timely manner, and for all necessary contacts with your
            insurance carrier.
          </Text>
          <Text style={styles.heading}>Proprietary Rights and Trademark License</Text>

          <Text>

            All intellectual property rights in the Shotgun Platform shall be owned by
            us absolutely and in their entirety. These rights include and are not
            limited to database rights, copyright, design rights (whether registered or
            unregistered), trademarks (whether registered or unregistered) and other
            similar rights wherever existing in the world together with the right to
            apply for protection of the same. All other trademarks, logos, service
            marks, company or product names set forth in the Shotgun Platform are the
            property of their respective owners. You acknowledge and agree that any
            questions, comments, suggestions, ideas, feedback or other information
            (“Submissions”) provided by you to us are non-confidential and shall become
            the sole property of Shotgun. Shotgun shall own exclusive rights, including
            all intellectual property rights, and shall be entitled to the unrestricted
            use and dissemination of these Submissions for any purpose, commercial or
            otherwise, without acknowledgment or compensation to you.

            Shotgun logos, designs, graphics, icons, scripts and service names are
            registered trademarks, trademarks or trade dress of Shotgun in the United
            Kingdom and/or other countries. If you provide Services as a Driver,
            Shotgun grants to you, during the term of this Agreement, and subject to
            your compliance with the terms and conditions of this Agreement, a limited,
            revocable, non-exclusive license to display and use the Shotgun Trademarks
            solely in connection with providing the Services through the Shotgun
            Platform (“License”). The License is non-transferable and non-assignable,
            and you shall not grant to any third party any right, permission, license
            or sublicense with respect to any of the rights granted hereunder without
            Shotgun’s prior written permission, which it may withhold in its sole
            discretion. The Shotgun Trademarks may not be used in any manner that is
            likely to cause confusion.

            You acknowledge that Shotgun is the owner and licensor of the Shotgun
            Trademarks, and that your use of the Shotgun Trademarks will confer no
            additional interest in or ownership of the Shotgun Trademark in you but
            rather inures to the benefit of Shotgun. You agree to use the Shotgun
            Trademarks strictly in accordance with Shotgun’s Trademark Usage
            Guidelines, as may be provided to you and revised from time to time, and to
            immediately cease any use that Shotgun determines to nonconforming or
            otherwise unacceptable.
          </Text>
          <Text>
            You agree that you will not:
          </Text>
          <Text>
            1. Create any materials that incorporate the Shotgun Trademarks or any
            derivatives of the Shotgun Marks other than as expressly approved by
            Shotgun in writing;
          </Text>
          <Text>
            2. Use the Shotgun Trademarks in any way that tends to impair their
            validity as proprietary trademarks, service marks, trade names or trade
            dress, or use the Shotgun trademarks other than in accordance with the
            terms, conditions and restrictions herein;
          </Text>
          <Text>
            3. Take any other action that would jeopardize or impair Shotgun’s rights
            as owner of the Shotgun Trademarks or the legality and/or enforceability of
            the Shotgun Trademarks, including, without limitation, challenging or
            opposing Shotgun’s ownership in the Shotgun Trademark;
          </Text>
          <Text>
            4. Apply for trademark registration or renewal of trademark registration of
            any of the Shotgun Trademarks, any derivative of the Shotgun Trademarks,
            any combination of the Shotgun Trademarks and any other name, or any
            trademark, service mark, trade name, symbol or word which is similar to the
            Shotgun Trademarks;
          </Text>
          <Text>
            5. Use the Shotgun Trademarks on or in connection with any product, service
            or activity that is in violation of any law, statute, government regulation
            or standard.

            Violation of any provision of this License may result in immediate
            termination of the License, in Shotgun’s sole discretion. If you create any
            materials bearing the Shotgun Trademarks (in violation of this Agreement or
            otherwise), you agree that upon their creation Shotgun exclusively owns all
            right, title and interest in and to such materials, including without
            limitation any modifications to the Shotgun Trademarks or derivative works
            based on the Shotgun Trademarks. You further agree to assign any interest
            or right you may have in such materials to Shotgun, and to provide
            information and execute any documents as reasonably requested by Shotgun to
            enable Shotgun to formalize such assignment.
          </Text>
          <Text style={styles.heading}>Disclaimers</Text>

          <Text>

            The following disclaimers are made on behalf of Shotgun, our affiliates,
            and each of our respective officers, directors, employees, agents,
            shareholders and suppliers.

            The Shotgun Platform is provided on an “as is” basis and without any
            warranty or condition, express, implied or statutory. We do not guarantee
            and do not promise any specific results from use of the Shotgun Platform
            and/or the Services, including the ability to provide or receive Services
            at any given location or time.

            We specifically disclaim any implied warranties of title, merchantability,
            fitness for a particular purpose and non-infringement. Some states do not
            allow the disclaimer of implied warranties, so the foregoing disclaimer may
            not apply to you. This warranty gives you specific legal rights and you may
            also have other legal rights that vary from state to state.

            We do not warrant that your use of the Shotgun Platform or Services will be
            accurate, complete, reliable, current, secure, uninterrupted, always
            available, or error-free, or will meet your requirements, that any defects
            in the Shotgun Platform will be corrected, or that the Shotgun Platform is
            free of viruses or other harmful components. We disclaim liability for, and
            no warranty is made with respect to, connectivity and availability of the
            Shotgun Platform or Services.

            We have no control over the quality or safety of the Service. We cannot
            ensure that a Partner or Customer will complete an arranged Service.

            We cannot guarantee that each Customer is who he or she claims to be.
            Please use common sense when using the Shotgun Platform and Services,
            including looking at the photos of the Partner or Customer you have matched
            with to make sure it is the same individual you see in person. Please note
            that there are also risks of dealing with underage persons or people acting
            under false pretence, and we do not accept responsibility or liability for
            any content, communication or other use or access of the Shotgun Platform
            by persons under the age of 18 in violation of this Agreement. We encourage
            you to communicate directly with each potential Partner or Customer prior
            to engaging in an arranged Service.

            Shotgun is not responsible for the conduct, whether online or offline, of
            any User of the Shotgun Platform or Services. You are solely responsible
            for your interactions with other Users. We do not procure insurance for,
            nor are we responsible for, personal belongings left in the vehicles by
            Partners or Customers. By using the Shotgun Platform and participating in
            the Services, you agree to accept such risks and agree that Shotgun is not
            responsible for the acts or omissions of Users on the Shotgun Platform or
            participating in the Services.

            It is possible for others to obtain information about you that you provide,
            publish or post to or through the Shotgun Platform (including any profile
            information you provide), send to other Users, or share during the
            Services, and to use such information to harass or harm you. We are not
            responsible for the use of any personal information that you disclose to
            other Users on the Shotgun Platform or through the Services. Please
            carefully select the type of information that you post on the Shotgun
            Platform or through the Services or release to others. We disclaim all
            liability, regardless of the form of action, for the acts or omissions of
            other Users (including unauthorized users, or “hackers”).

            Opinions, advice, statements, offers, or other information or content made
            available through the Shotgun Platform, but not directly by us, are those
            of their respective authors, and should not necessarily be relied upon.
            Such authors are solely responsible for such content. Under no
            circumstances will we be responsible for any loss or damage resulting from
            your reliance on information or other content posted on the Shotgun
            Platform or otherwise disseminated by third parties. We reserve the right,
            but we have no obligation, to monitor the materials posted in the public
            areas of the Shotgun Platform and remove any such material that in our sole
            opinion violates, or is alleged to violate, the law or this agreement or
            which might be offensive, illegal, or that might violate the rights, harm,
            or threaten the safety of Users or others.

            The Shotgun Platform contains (or you may be sent through the Shotgun
            Platform) links to other web sites owned and operated by third parties
            (“Third Party Sites”), as well as articles, photographs, text, graphics,
            pictures, designs, music, sound, video, information, applications, software
            and other content or items belonging to or originating from third parties
            (“Third Party Content”). Such Third Party Sites and Third Party Content are
            not investigated, monitored or checked for accuracy, appropriateness, or
            completeness by us, and we are not responsible for any Third Party Sites or
            Third Party Content accessed through the Shotgun Platform.

            Location data provided by the Shotgun Platform is for basic location
            purposes only and is not intended to be relied upon in situations where
            precise location information is needed or where erroneous, inaccurate or
            incomplete location data may lead to death, personal injury, property or
            environmental damage. Neither Shotgun, nor any of its content providers,
            guarantees the availability, accuracy, completeness, reliability, or
            timeliness of location data displayed by the Shotgun Platform. Any of your
            Information, including geo-locational data, you upload, provide, or post on
            the Shotgun Platform may be accessible to Shotgun and certain Users of the
            Shotgun Platform.

            This paragraph applies to any version of the Shotgun Platform that you
            acquire from the Apple App Store. This Agreement is entered into between
            you and Shotgun. Apple, Inc. (“Apple”) is not a party to this Agreement and
            shall have no obligations with respect to the Shotgun Platform. Shotgun,
            not Apple, is solely responsible for the Shotgun Platform and the content
            thereof as set forth hereunder. However, Apple and Apple’s subsidiaries are
            third party beneficiaries of this Agreement. Upon your acceptance of this
            Agreement, Apple shall have the right (and will be deemed to have accepted
            the right) to enforce this Agreement against you as a third party
            beneficiary thereof. This Agreement incorporates by reference the Licensed
            Application End User License Agreement published by Apple at
            http://www.apple.com/legal/internet-services/itunes/appstore/dev/stdeula/,
            for purposes of which, you are “the end-user.” In the event of a conflict
            in the terms of the Licensed Application End User License Agreement and
            this Agreement, the terms of this Agreement shall control.

            State and Local Disclosures

            Certain jurisdictions require additional disclosures to you. You can view
            any disclosures required by your local jurisdiction at ………….. We will
            update the disclosures page as jurisdictions add, remove or amend these
            required disclosures, so please check in regularly for updates.

            Indemnity

            You will defend, indemnify, and hold us and our affiliates and each of our
            respective officers, directors, employees, agents, shareholders and
            suppliers harmless from any claims, actions, suits, losses, costs,
            liabilities and expenses (including reasonable attorneys’ fees) relating to
            or arising out of your use of the Shotgun Platform and participation in the
            Services, including:

            1 Your breach of this Agreement or the documents it incorporates by
            reference;

            2 Your violation of any law or the rights of a third party, including,
            without limitation, Partners, Customers, other motorists, and pedestrians,
            as a result of your own interaction with such third party;

            3 Any allegation that any materials that you submit to us or transmit
            through the Shotgun Platform or to us infringe or otherwise violate the
            copyright, trademark, trade secret or other intellectual property or other
            rights of any third party;

            4 Your ownership, use or operation of a motor vehicle or passenger vehicle,
            including your provision of Services as a Driver; and/or

            5 Any other activities in connection with the Services. This indemnity
            shall be applicable without regard to the negligence of any party,
            including any indemnified person.
          </Text>
          <Text>
            Limitation of Liability

            <Text style={styles.heading}>
              IN NO EVENT WILL WE, OUR AFFILIATES, OR EACH OF OUR RESPECTIVE
              OFFICERS, DIRECTORS, EMPLOYEES, AGENTS, SHAREHOLDERS OR SUPPLIERS, BE
              LIABLE TO YOU FOR ANY INCIDENTAL, SPECIAL, PUNITIVE, CONSEQUENTIAL, OR
              INDIRECT DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR DELETION,
              CORRUPTION, LOSS OF DATA, LOSS OF PROGRAMS, FAILURE TO STORE ANY
              INFORMATION OR OTHER CONTENT MAINTAINED OR TRANSMITTED BY THE SHOTGUN
              PLATFORM, SERVICE INTERRUPTIONS, OR FOR THE COST OF PROCUREMENT OF
              SUBSTITUTE SERVICES) ARISING OUT OF OR IN CONNECTION WITH THE SHOTGUN
              PLATFORM, THE SERVICES, OR THIS AGREEMENT, HOWEVER ARISING INCLUDING
              NEGLIGENCE, EVEN IF WE OR OUR AGENTS OR REPRESENTATIVES KNOW OR HAVE
              BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES (PROVIDED HOWEVER THAT
              THIS PROVISION SHALL NOT LIMIT THE SCOPE OF SHOTGUN’S INSURANCE
              POLICIES WHICH CAN BE PROVIDED ON REQUEST. WE WILL NOT BE LIABLE FOR
              ANY DAMAGES, DIRECT, INDIRECT, SPECIAL, PUNITIVE, INCIDENTAL AND/OR
              CONSEQUENTIAL (INCLUDING, BUT NOT LIMITED TO PHYSICAL DAMAGES, BODILY
              INJURY, DEATH AND/OR EMOTIONAL DISTRESS AND DISCOMFORT) ARISING OUT OF
              YOUR COMMUNICATING WITH OR MEETING OTHER USERS OF THE SHOTGUN PLATFORM
              OR SERVICES, EVEN IF WE OR OUR AGENTS OR REPRESENTATIVES KNOW OR HAVE
              BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. CERTAIN JURISDICTIONS
              MAY NOT ALLOW THE EXCLUSION OR LIMITATION OF CERTAIN DAMAGES. IF THESE
              LAWS APPLY TO YOU, SOME OR ALL OF THE ABOVE DISCLAIMERS, EXCLUSIONS OR
              LIMITATIONS MAY NOT APPLY TO YOU, AND YOU MAY HAVE ADDITIONAL RIGHTS.
            </Text>
          </Text>
          <Text style={styles.heading}>Release</Text>

          <Text>

            In the event that you have a dispute with one or more Users, you agree to
            release Shotgun (including our affiliates and each of our respective
            officers, directors, employees, agents, shareholders, and suppliers) from
            claims, demands and damages of every kind and nature, known and unknown,
            suspected and unsuspected, disclosed and undisclosed, arising out of or in
            any way connected to such disputes with other Users or to your use of the
            Shotgun Platform or participation in the Services. Furthermore, you
            expressly waive any rights you may have under California Civil Code Section
            1542 (or analogous laws of other states), which says: “A general release
            does not extend to claims which the creditor does not know or suspect to
            exist in his favor at the time of executing the release, which, if known by
            him must have materially affected his settlement with the debtor.” We
            reserve the right, but have no obligation, to monitor disputes between you
            and other Users.

            Term and Termination

            This Agreement is effective upon your creation of a User account, as
            amended by any modifications made pursuant to Section 1. You may
            discontinue your use of the Shotgun Platform or participation in the
            Services at any time, for any reason. We may suspend or deactivate your
            User account (either as a Customer and/or Driver), or revoke your
            permission to access the Shotgun Platform, at any time, for any reason,
            upon notice to you. We reserve the right to refuse access to the Shotgun
            Platform to any User for any reason not prohibited by law. Either party may
            terminate the Agreement for any reason upon written notice to the other
            party. Sections 1, 4 (with respect to the license), 6-11 and 13-21 shall
            survive any termination or expiration of this Agreement.

            Agreement to Arbitrate All Disputes and Legal Claims

            You and We agree that any legal disputes or claims arising out of or
            related to the Agreement (including but not limited to the use of the
            Shotgun Platform and/or the Services, or the interpretation,
            enforceability, revocability, or validity of the Agreement, or the
            arbitrability of any dispute), that cannot be resolved informally shall be
            submitted to binding arbitration in the state in which the Agreement was
            performed. The arbitration shall be conducted by the Arbitration
            Association under its Commercial Arbitration Rules, or as otherwise
            mutually agreed by you and we. Any judgment on the award rendered by the
            arbitrator may be entered in any court having jurisdiction thereof. Claims
            shall be brought within the time required by applicable law. You and we
            agree that any claim, action or proceeding arising out of or related to the
            Agreement must be brought in your individual capacity, and not as a
            plaintiff or class member in any purported class, collective, or
            representative proceeding. The arbitrator may not consolidate more than one
            person’s claims, and may not otherwise preside over any form of a
            representative, collective, or class proceeding.

            <Text style={styles.heading}>
              YOU ACKNOWLEDGE AND AGREE THAT YOU AND SHOTGUN ARE EACH WAIVING THE
              RIGHT TO A TRIAL BY JURY OR TO PARTICIPATE AS A PLAINTIFF OR CLASS
              MEMBER IN ANY PURPORTED CLASS ACTION OR REPRESENTATIVE PROCEEDING.
            </Text>
          </Text>
          <Text style={styles.heading}>Confidentiality</Text>
          <Text>

            You agree not to use any technical, financial, strategic and other
            proprietary and confidential information relating to Shotgun’s business,
            operations and properties, including User information (“Confidential
            Information”) disclosed to you by Shotgun for your own use or for any
            purpose other than as contemplated herein. You shall not disclose or permit
            disclosure of any Confidential Information to third parties. You agree to
            take all reasonable measures to protect the secrecy of and avoid disclosure
            or use of Confidential Information of Shotgun in order to prevent it from
            falling into the public domain. Notwithstanding the above, you shall not
            have liability to Shotgun with regard to any Confidential Information which
            you can prove: was in the public domain at the time it was disclosed by
            Shotgun or has entered the public domain through no fault of yours; was
            known to you, without restriction, at the time of disclosure, as
            demonstrated by files in existence at the time of disclosure; is disclosed
            with the prior written approval of Shotgun; becomes known to you, without
            restriction, from a source other than Shotgun without breach of this
            Agreement by you and otherwise not in violation of Shotgun’s rights; or is
            disclosed pursuant to the order or requirement of a court, administrative
            agency, or other governmental body; provided, however, that You shall
            provide prompt notice of such court order or requirement to Shotgun to
            enable Shotgun to seek a protective order or otherwise prevent or restrict
            such disclosure.
          </Text>
          <Text style={styles.heading}>
            No Agency
          </Text>
          <Text>
            You and Shotgun are independent contractors, and no agency, partnership,
            joint venture, employee-employer or franchisor-franchisee relationship is
            intended or created by this Agreement.
          </Text>
          <Text>
            To resolve a complaint regarding the Shotgun Platform, you should contact
            us via the feedback function within the Shotgun App
          </Text>
          <Text style={styles.heading}>General</Text>
          <Text>
            This Agreement shall be governed by the laws of the United Kingdom without
            regard to choice of law principles. If any provision of this Agreement is
            held to be invalid or unenforceable, such provision shall be struck and the
            remaining provisions shall be enforced. You agree that this Agreement and
            all incorporated agreements may be automatically assigned by Shotgun, in
            our sole discretion in accordance with the “Notices” section of this
            Agreement. Headings are for reference purposes only and in no way define,
            limit, construe or describe the scope or extent of such section. A party’s
            failure to act with respect to a breach by the other party does not
            constitute a waiver of the party’s right to act with respect to subsequent
            or similar breaches. This Agreement sets forth the entire understanding and
            agreement between you and Shotgun with respect to the subject matter
            hereof.
          </Text>
          <Text style={styles.heading}>
            Privacy Policy
          </Text>
          <Text>
            Shotgun is dedicated to protecting your personal information and informing
            you about how we use it. This privacy policy applies to transactions and
            activities and data gathered through the Shotgun Platform. Please review
            this privacy policy periodically as we may revise it without notice. This
            privacy policy was last revised on 29 March 2018. Each time you use the
            Shotgun Platform or provide us with information, by doing so you are
            accepting the practices described in this privacy policy at that time.
          </Text>
          <Text style={styles.heading}>
            Data We Collect From You
          </Text>
          <Text>
            In order to operate the Shotgun Platform and to provide You with
            information about products or services that may be of interest to You, We
            may collect “personal information” (i.e. information that could be used to
            contact You directly (without using the Shotgun Platform) such as full
            name, postal address, phone number, credit/debit card information, or email
            address) or “demographic information” (i.e. information that You submit, or
            that We collect, that is not personal information; this may include, but is
            not limited to, zip code, hometown, gender, username, age/birth date). You
            represent and warrant that You have the authority to provide Us with any
            such contact information. Demographic information is divided into two
            categories:
          </Text>
          <Text>
            1 “Non-public information”, which consists of Service transaction
            information (not including sensitive payment information); and
          </Text>
          <Text>
            2 “Public information”, which consists of all other demographic
            information.
          </Text>
          <Text>
            Please note that nowhere on the Shotgun Platform do We knowingly collect,
            keep or maintain personal information from children under the age of 18, as
            We require that all users represent to Us that they are at least 18 years
            old.
          </Text>
          <Text style={styles.heading}>How We Use Personal Information</Text>
          <Text>
            We use Your email address and Your other personal information to help Us
            efficiently operate the Shotgun Platform, to contact You in connection with
            Your transactions and other activities on the Shotgun Platform (including,
            but not limited to, confirmation emails, or important news that could
            affect Your relationship with Shotgun), to forward trip information to You
            from other Users, to forward trip information from You to other Users, and
            to contact You and others to suggest potential matches. We use your contact
            information to find and connect with Your friends (when instructed by You).
            These types of communications are known as “Operational Communications.” In
            some cases, Operational Communications may also contain commercial
            messages, such as banner ads and special offers.
          </Text>
          <Text>
            To operate the Shotgun Platform, including processing Your transactions and
            supporting Your activities on the Shotgun Platform, We may share Your
            personal information with Our agents, representatives, contractors and
            service providers so they can provide Us with support services such as
            email origination, receipt or support services, customer relationship
            management services, and order fulfilment. We require these entities not to
            use your information for any other purpose.
          </Text>
          <Text>
            Any third party with whom We are allowed to share Your personal information
            is authorized to use Your personal information in accordance with Our
            contractual arrangements with such third parties and in accordance with
            their own privacy policies, over which We have no control, and you agree
            that We are not responsible or liable for any of their actions or
            omissions. Those who contact you will need to be instructed directly by you
            regarding your preferences for the use of Your personal information by
            them.
          </Text>
          <Text style={styles.heading}>How We Use Demographic Data</Text>
          <Text>
            We may review all demographic Data. We may use public information to enable
            other users to search your profile, to determine whether Your trip details
            fit other user’s requirements, and to communicate with You. We may use
            demographic information to tailor the Shotgun Platform and communications
            to your interests. We may also share demographic information with
            advertisers on an anonymous and aggregated basis (i.e., without telling the
            advertisers Your identity). One of the reasons we may do this is to
            increase the likelihood that Our advertisers’ goods and services will
            appeal to You as a user of the Shotgun Platform. Our sharing of demographic
            information with advertisers is anonymous (i.e., We do not tell advertisers
            which particular Shotgun Users are members of which demographic groups),
            subject to the rest of this privacy policy. When You respond to an
            advertisement, however, We ask You to remember that if that ad that is
            targeted to a demographic group and You decide to give the advertiser Your
            personal information, then the advertiser may be able to identify You as
            being a member of that demographic group.
          </Text>
          <Text style={styles.heading}>How to Edit Your Information</Text>
          <Text>
            Shotgun provides you with the ability to access and edit Your personal
            information. To update your personal info, click the Settings tab in the
            Shotgun App. There you can view, update and correct your account
            information.
          </Text>
          <Text>
            Our databases automatically update any personal information you edit in
            your profile, or that you request we edit. Information transmitted through
            boards, chats, polls, or through any other means remain in our databases
            and become the property of Shotgun upon submission. Keep this in mind if
            you decide to communicate personal information through any of these
            applications.
          </Text>
          <Text style={styles.heading}>Information Retention</Text>
          <Text>
            To preserve the integrity of our databases, standard procedure calls for us
            to retain information submitted by members for an indefinite length of
            time. Shotgun understands your submissions as consent to store all your
            information in one place for this indefinite length of time, if we so wish.
            If required by law, as is the case to comply with the Children’s Online
            Privacy Protection Act (COPPA), we will nullify member information by
            erasing it from Our database. We will also respond to written member
            requests to nullify account information. Also, by using the Shotgun
            Platform, You do hereby represent and warrant that You understand and agree
            that all information submitted by You through the Shotgun Platform or
            otherwise to Shotgun becomes the property of Shotgun and may be used in the
            sole discretion of Shotgun in accordance with this Privacy Policy and the
            Terms of Use.
          </Text>
          <Text style={styles.heading}>Special Cases in Which We Share Personal Information</Text>
          <Text>
            Your personal information may be passed on to a third party in the event of
            a transfer of ownership or assets, or a bankruptcy. We may also disclose
            personal information when we determine that such disclosure is necessary to
            comply with applicable law, to cooperate with law enforcement or to protect
            the interests or safety of Shotgun or other visitors to the Shotgun
            Platform. We also may disclose your personal information to our subsidiary
            and parent companies and businesses, and other affiliated legal entities
            and businesses with who we are under common corporate control. Whenever
            personal information is disclosed under this paragraph, we may also
            disclose your demographic information along with it, on a non-anonymous
            basis. All of our parent, subsidiary and affiliated legal entities and
            businesses that receive Your personal information or non-anonymous
            demographic information from us will comply with the terms of this privacy
            policy with respect to their use and disclosure of such information.
          </Text>
          <Text style={styles.heading}>Our Security Precautions</Text>
          <Text>
            Your Shotgun Profile is password-protected so that only you and authorized
            Shotgun employees have access to your account information. In order to
            maintain this protection, do not give your password to anyone. Shotgun
            staff will never proactively reach out to you and ask for any personal
            account information, including your password.
          </Text>
          <Text>
            Shotgun makes every effort to ensure that your information is secure on its
            system. Shotgun has staff dedicated to maintaining Our privacy policy as
            set forth herein and other privacy initiatives, periodically reviewing Web
            security and making sure that every Shotgun employee is aware of Our
            security practices. Unfortunately, no data transmission over the Internet
            can be guaranteed to be 100% secure. As a result, Shotgun cannot guarantee
            the security of any information you transmit to us, and you do so at your
            own risk. If you have any further questions on this issue, refer to Shotgun
            Terms of Use. Shotgun expressly disclaims any liability that may arise
            should any other individuals obtain the information you submit to the
            Shotgun Platform.
          </Text>
          <Text>
            Shotgun has security measures in place to protect against the loss, misuse
            and alteration of the information under our control. Your information may
            be transferred to and maintained on computer networks which may be located
            outside of the country or other governmental jurisdiction in which you
            reside, and the country or jurisdiction in which these computer networks
            are located may not have privacy laws as protective as the laws in Your
            country or jurisdiction.
          </Text>
          <Text>
            The Shotgun Platform may contain links to other web sites. We are of course
            not responsible for the privacy practices of other web sites. We encourage
            Our Users to be aware when they leave the Shotgun Platform to read the
            privacy statements of each and every web site that collects personally
            identifiable information. This Privacy Policy applies solely to information
            collected by the Shotgun Platform.
          </Text>
          <Text style={styles.heading}>
            Changing our Privacy Policy for Previously Gathered Information
          </Text>
          <Text>
            If at any point We decide to use particular personally identifiable
            information in a manner materially different from that stated at the time
            it was collected, We will notify Users by way of an email or by providing
            30 days notice on the Shotgun Platform. We also encourage you to review
            this privacy policy periodically. By using the Shotgun Platform, you do
            hereby represent and warrant that you have read, understand and agree to
            all terms of Agreement. Each time you use the Shotgun Platform, you agree
            to all terms set forth in this Agreement and any other policies published
            by Shotgun on the Shotgun Platform. Please note that we will continue to
            have the right to change our privacy policy and practices, and how we use
            Your personally identifiable information, without notice, as described in
            herein, provided that such changes shall only apply to information gathered
            on or after the date of the change.
          </Text>
          <Text style={styles.heading}>
            Contacting Shotgun
          </Text>
          <Text>
            If you have any questions about this privacy statement, the practices of
            Shotgun, or your dealings with Shotgun, you may contact us at our via the
            Contact us facility in the Shotgun App
          </Text>
        </Col>
      </Grid>
      <Text>
        Payment processing services for drivers and customers on Shotgun are provided by Stripe and are subject to the
        Stripe Connected Account Agreement,
        which includes the Stripe Terms of Service (collectively, the “Stripe Services Agreement”). By agreeing to these
        terms or continuing to operate as
        a driver or customer on Shotgun, you agree to be bound by the Stripe Services Agreement, as the same may be
        modified by Stripe from time to time.
        As a condition of Shotgun enabling payment processing services through Stripe, you agree to provide Shotgun
        accurate and complete information about you and your business,
        and you authorize Shotgun to share it and transaction information related to your use of the payment processing
        services provided by Stripe.
      </Text>
    </Content>
  </Container>;
};

styles = {
  heading: {
    fontWeight: 'bold',
    marginTop: 10,
    marginBottom: 15
  }
}
